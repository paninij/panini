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

package org.paninij.proc.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.paninij.lang.Block;
import org.paninij.lang.Duck;
import org.paninij.lang.Future;

public class ProcedureElement implements Procedure
{
    private ExecutableElement element;
    private AnnotationKind annotationKind;
    private Type returnType;
    private String name;
    private List<Variable> parameters;

    public ProcedureElement(ExecutableElement e) {
        super();
        this.element = e;
        this.annotationKind = null;
        this.returnType = new Type(this.element.getReturnType());
        this.parameters = null;
        this.name = e.getSimpleName().toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public AnnotationKind getAnnotationKind() {
        if (this.annotationKind != null) return this.annotationKind;

        if (this.element.getAnnotation(Future.class) != null) {
            this.annotationKind = AnnotationKind.FUTURE;
        } else if (this.element.getAnnotation(Block.class) != null) {
            this.annotationKind = AnnotationKind.BLOCK;
        } else if (this.element.getAnnotation(Duck.class) != null) {
            this.annotationKind = AnnotationKind.DUCKFUTURE;
        } else {
            this.annotationKind = AnnotationKind.NONE;
        }

        return this.annotationKind;
    }

    @Override
    public Type getReturnType() {
        return this.returnType;
    }

    @Override
    public List<Variable> getParameters() {
        if (this.parameters != null) return this.parameters;

        this.parameters = new ArrayList<Variable>();

        Iterator<? extends VariableElement> it = this.element.getParameters().iterator();
        while (it.hasNext()) {
            VariableElement param = it.next();
            
            boolean vararg = this.element.isVarArgs() && !it.hasNext();
            Variable v = new Variable(param.asType(), param.toString(), vararg);
            this.parameters.add(v);
        }

        return this.parameters;
    }

    @Override
    public String toString() {
        String str = this.getReturnType() + " " + this.getName() + "(";
        String args = "";
        for (Variable v : this.getParameters()) {
            args += (v + ", ");
        }
        args = args.length() > 1 ? args.substring(0, args.length() - 2) : "";
        str += args + ")";
        return str;
    }

    @Override
    public List<String> getModifiers() {
        List<String> modifiers = new ArrayList<String>();
        for (Modifier m : this.element.getModifiers()) {
            modifiers.add(m.toString());
        }
        return modifiers;
    }

    @Override
    public List<String> getThrown() {
        List<String> thrown = new ArrayList<String>();
        for (TypeMirror m : this.element.getThrownTypes()) {
            thrown.add(m.toString());
        }
        return thrown;
    }
}
