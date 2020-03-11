package org.goflex.wp2.core.wrappers;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.goflex.wp2.core.entities.ConsumptionTuple;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;

/**
 * TODO: TempWrapper for INEA
 *
 * @author Bijay
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationInformation implements Serializable {


    private static final long serialVersionUID = 7959016934179550864L;

    private int operationState = 1;

    private ConsumptionTuple[] operationPower;

    private ConsumptionTuple[] operationPrognosis;

    public int getOperationState() {
        return operationState;
    }

    public void setOperationState(int operationState) {
        this.operationState = operationState;
    }


    public ConsumptionTuple[] getOperationPower() {
        return operationPower;
    }

    public void setOperationPower(ConsumptionTuple[] operationPower) {
        this.operationPower = operationPower;
    }

    public ConsumptionTuple[] getOperationPrognosis() {
        return operationPrognosis;
    }

    public void setOperationPrognosis(ConsumptionTuple[] operationPrognosis) {
        this.operationPrognosis = operationPrognosis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperationInformation)) return false;

        OperationInformation that = (OperationInformation) o;

        if (operationState != that.operationState) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(operationPower, that.operationPower)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(operationPrognosis, that.operationPrognosis);
    }

    @Override
    public int hashCode() {
        int result = operationState;
        result = 31 * result + Arrays.hashCode(operationPower);
        result = 31 * result + Arrays.hashCode(operationPrognosis);
        return result;
    }

    @Override
    public String toString() {
        return "OperationInformation{" +
                "operationState=" + operationState +
                ", operationPower=" + Arrays.toString(operationPower) +
                ", operationPrognosis=" + Arrays.toString(operationPrognosis) +
                '}';
    }

}
