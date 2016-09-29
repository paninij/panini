package org.paninij.proc;

import org.paninij.lang.CapsuleInterface;
import org.paninij.lang.SignatureInterface;
import org.paninij.proc.check.Check.Result;
import org.paninij.proc.check.CheckException;
import org.paninij.proc.check.capsule.CapsuleCheck;
import org.paninij.proc.check.capsule.CheckForCycleOfLocalFields;
import org.paninij.proc.check.capsule.RoundOneCapsuleChecks;
import org.paninij.proc.factory.CapsuleMonitorFactory;
import org.paninij.proc.factory.CapsuleSerialFactory;
import org.paninij.proc.factory.CapsuleTaskFactory;
import org.paninij.proc.factory.CapsuleThreadFactory;
import org.paninij.proc.factory.MessageFactory;
import org.paninij.proc.model.Capsule;
import org.paninij.proc.model.CapsuleElement;
import org.paninij.proc.model.Procedure;
import org.paninij.proc.model.Signature;
import org.paninij.proc.model.SignatureElement;
import org.paninij.proc.util.ArtifactFiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

import static org.paninij.proc.util.PaniniModel.CAPSULE_TEMPLATE_SUFFIX;
import static org.paninij.proc.util.PaniniModel.SIGNATURE_TEMPLATE_SUFFIX;

/**
 * @author dwtj
 */
@SupportedAnnotationTypes({"org.paninij.lang.CapsuleInterface",
                           "org.paninij.lang.SignatureInterface"})
@SupportedOptions({})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RoundOneProcessor extends AbstractProcessor {

    // Standard utilities from the processing environment instance:
    private Types typeUtils;
    private Elements elementUtils;

    // Custom utilities instantiated using the processing environment instance:
    private RoundOneCapsuleChecks capsuleCheck;
    private CapsuleCheck cycleCheck;
    private ArtifactFiler artifactMaker;


    // Factories to perform code generating:
    private final MessageFactory messageFactory = new MessageFactory();
    private final CapsuleThreadFactory capsuleThreadFactory = new CapsuleThreadFactory();
    private final CapsuleSerialFactory capsuleSerialFactory = new CapsuleSerialFactory();
    private final CapsuleMonitorFactory capsuleMonitorFactory = new CapsuleMonitorFactory();
    private final CapsuleTaskFactory capsuleTaskFactory = new CapsuleTaskFactory();

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();

        capsuleCheck = new RoundOneCapsuleChecks(processingEnv);
        cycleCheck = new CheckForCycleOfLocalFields(processingEnv);
        artifactMaker = new ArtifactFiler(processingEnv.getFiler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver()) {
            artifactMaker.close();
            return false;
        }

        // TODO: What about if an error was raised in the prior round? See: roundEnv.errorRaised()

        // Perform all remaining code-gen on OK capsule templates:
        for (TypeElement template : getOkCapsuleTemplates(roundEnv)) {
            Capsule model = CapsuleElement.make(template);
            for (Procedure procedure : model.getProcedures()) {
                artifactMaker.add(messageFactory.make(procedure));
            }
            artifactMaker.add(capsuleThreadFactory.make(model));
            artifactMaker.add(capsuleSerialFactory.make(model));
            artifactMaker.add(capsuleMonitorFactory.make(model));
            artifactMaker.add(capsuleTaskFactory.make(model));
        }

        // Perform all remaining code-gen on OK signature templates:
        for (TypeElement template : getOkSignatureTemplates(roundEnv)) {
            Signature model = SignatureElement.make(template);
            for (Procedure procedure : model.getProcedures()) {
                artifactMaker.add(messageFactory.make(procedure));
            }
        }

        artifactMaker.makeAll();
        return false;
    }

    /**
     * <p>Get all OK capsule templates whose capsule interfaces were generated in the last round. We
     * consider a capsule template to be OK if it passes all checks (i.e. all checks return an OK
     * {@link Result}).
     *
     * <p>If some capsule template whose capsule interface was generated in the last round does not
     * pass some check, then this method has the side effect of reporting this failed check via
     * {@link #error}.
     *
     * <p>Code generation ought to be able to be performed on all capsule templates returned from
     * this method.
     *
     * @param roundEnv
     *          The current round environment, in which we lookup capsule interfaces generated in
     *          the last round.
     * @return
     *          A newly instantiated set of type elements of capsule templates adhering to the
     *          above description.
     */
    private Set<TypeElement> getOkCapsuleTemplates(RoundEnvironment roundEnv) {
        Set<TypeElement> set = new HashSet<>();

        for (Element iface : roundEnv.getElementsAnnotatedWith(CapsuleInterface.class)) {
            String templateName = iface + CAPSULE_TEMPLATE_SUFFIX;
            TypeElement template = elementUtils.getTypeElement(templateName);
            if (template == null) {
                String msg = "Found a capsule interface, but could not find its corresponding "
                           + "capsule template: " + templateName;
                throw new IllegalStateException(msg);
            }

            Result result = capsuleCheck.checkCapsule(template);
            if (!result.ok()) {
                // TODO: Re-enable once compile test improvements make `CheckException` obsolete.
                //error(result.errMsg(), result.offender());
                throw new CheckException();
            } else {
                set.add(template);
            }
        }

        // Run one more check. This check is not part of the `capsuleCheck` above, because some of
        // the prior checks need to have already been performed before this check will behave
        // correctly.
        for (TypeElement template : set) {
            Result result = cycleCheck.checkCapsule(template);
            if (!result.ok()) {
                set.remove(template);
                throw new CheckException();
            }
        }

        return set;
    }

    /**
     * Just like {@link #getOkCapsuleTemplates(RoundEnvironment)} but for signature templates.
     */
    private Set<TypeElement> getOkSignatureTemplates(RoundEnvironment roundEnv) {
        // Note: In the current implementation, all signature template checks will have already been
        // performed in the previous round, and no more checks need to be performed here.
        Set<TypeElement> set = new HashSet<>();
        for (Element iface : roundEnv.getElementsAnnotatedWith(SignatureInterface.class)) {
            String templateName = iface + SIGNATURE_TEMPLATE_SUFFIX;
            TypeElement template = elementUtils.getTypeElement(templateName);
            if (template == null) {
                String msg = "Found a signature interface, but could not find its corresponding "
                           + "signature template: " + templateName;
                throw new IllegalStateException(msg);
            }
            set.add(template);
        }
        return set;
    }

    public void error(String msg) {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, msg);
    }

    public void error(String msg, Element offender) {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, msg, offender);
    }
}
