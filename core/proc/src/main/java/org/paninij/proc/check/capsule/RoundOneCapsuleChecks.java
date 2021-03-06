/*******************************************************************************
 * This file is part of the Panini project at Iowa State University.
 *
 * @PaniniJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * @PaniniJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with @PaniniJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more details and the latest version of this code please see
 * http://paninij.org
 *
 * Contributors:
 * 	Dr. Hridesh Rajan,
 * 	Dalton Mills,
 * 	David Johnston,
 * 	Trey Erenberger
 *******************************************************************************/

package org.paninij.proc.check.capsule;

import static org.paninij.proc.check.Check.Result.OK;
import static org.paninij.proc.util.JavaModel.isAnnotatedBy;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.paninij.proc.check.capsule.decl.CheckDesignDecl;
import org.paninij.proc.check.capsule.decl.CheckInitDecl;
import org.paninij.proc.check.capsule.decl.CheckRunDecl;
import org.paninij.proc.check.core.CheckForIllegalMethodNames;
import org.paninij.proc.check.core.CheckForNestedTypes;
import org.paninij.proc.check.core.CheckForTypeParameters;
import org.paninij.proc.check.core.CheckForIllegalSubtyping;
import org.paninij.proc.check.core.CheckProcAnnotations;
import org.paninij.proc.check.core.CheckForBadAnnotations;


public class RoundOneCapsuleChecks implements CapsuleCheck
{
    protected final ProcessingEnvironment procEnv;
    protected final CapsuleCheck capsuleChecks[];

    public RoundOneCapsuleChecks(ProcessingEnvironment procEnv)
    {
        this.procEnv = procEnv;
        this.capsuleChecks = new CapsuleCheck[]
        {

            new CheckFields(procEnv),
            new CheckForImportedFieldsOnRoot(),
            new CheckForLocalSignatureFields(procEnv),
        };
    }

    public Result checkCapsule(TypeElement core)
    {
        if (! isAnnotatedBy(procEnv, core, "org.paninij.lang.Capsule")) {
            String err = "Tried to check an element as a capsule core, but it is not annotated "
                       + "with `@Capsule`: " + core;
            throw new IllegalArgumentException(err);
        }
        for (CapsuleCheck check: capsuleChecks) {
            Result result = check.checkCapsule(core);
            if (!result.ok()) {
                return result;
            }
        }
        return OK;
    }

}
