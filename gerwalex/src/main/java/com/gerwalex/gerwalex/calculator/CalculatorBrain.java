package com.gerwalex.gerwalex.calculator;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by alex on 11.03.2016.
 */
public class CalculatorBrain {
    // operator types
    private static final String ADD = "+";
    private static final String SUBTRACT = "-";
    private static final String MULTIPLY = "*";
    private static final String DIVIDE = "/";
    private static final String CLEAR = "C";
    private static final String CLEARMEMORY = "MC";
    private static final String ADDTOMEMORY = "M+";
    private static final String SUBTRACTFROMMEMORY = "M-";
    private static final String RECALLMEMORY = "MR";
    private static final String SQUAREROOT = "√";
    private static final String SQUARED = "x²";
    private static final String INVERT = "1/x";
    private static final String TOGGLESIGN = "+/-";
    private static final String SINE = "sin";
    private static final String COSINE = "cos";
    private static final String TANGENT = "tan";
    private static final MathContext mathContext = new MathContext(8);
    private BigDecimal mCalculatorMemory;
    // 3 + 6 = 9
    // 3 & 6 are called the operand.
    // The + is called the operator.
    // 9 is the result of the operation.
    private BigDecimal mOperand = new BigDecimal(0);
    private BigDecimal mWaitingOperand = new BigDecimal(0);
    private String mWaitingOperator = "";
    // public static final String EQUALS = "=";

    public CalculatorBrain(BigDecimal initialValue) {
        if (initialValue != null) {
            mOperand = initialValue;
        }
    }

    // used on screen orientation change
    public BigDecimal getMemory() {
        return mCalculatorMemory;
    }

    // used on screen orientation change
    public void setMemory(BigDecimal calculatorMemory) {
        mCalculatorMemory = calculatorMemory;
    }

    public BigDecimal getResult() {
        return mOperand;
    }

    protected BigDecimal performOperation(String operator) {
        switch (operator) {
            case CLEAR:
                mOperand = new BigDecimal(0);
                mWaitingOperator = "";
                mWaitingOperand = new BigDecimal(0);
                // mCalculatorMemory = 0;
                break;
            case CLEARMEMORY:
                mCalculatorMemory = new BigDecimal(0);
                break;
            case ADDTOMEMORY:
                mCalculatorMemory = mCalculatorMemory.add(mOperand);
                break;
            case SUBTRACTFROMMEMORY:
                mCalculatorMemory = mCalculatorMemory.subtract(mOperand);
                break;
            case RECALLMEMORY:
                mOperand = mCalculatorMemory;
                break;
            case SQUAREROOT:
                mOperand = new BigDecimal(Math.sqrt(mOperand.doubleValue()));
                break;
            case SQUARED:
                mOperand = mOperand.multiply(mOperand);
                break;
            case INVERT:
                if (mOperand.doubleValue() != 0) {
                    mOperand = new BigDecimal(1).divide(mOperand, mathContext);
                }
                break;
            case TOGGLESIGN:
                mOperand = new BigDecimal(0).subtract(mOperand);
                break;
            case SINE:
                mOperand = new BigDecimal(Math.sin(Math.toRadians(
                        mOperand.doubleValue()))); // Math.toRadians(mOperand) converts result to
                // degrees
                break;
            case COSINE:
                mOperand = new BigDecimal(Math.cos(Math.toRadians(
                        mOperand.doubleValue()))); // Math.toRadians(mOperand) converts result to
                // degrees
                break;
            case TANGENT:
                mOperand = new BigDecimal(Math.tan(Math.toRadians(
                        mOperand.doubleValue()))); // Math.toRadians(mOperand) converts result to
                // degrees
                break;
            default:
                performWaitingOperation();
                mWaitingOperator = operator;
                mWaitingOperand = mOperand;
                break;
        }
        return mOperand;
    }

    protected void performWaitingOperation() {
        switch (mWaitingOperator) {
            case ADD:
                mOperand = mWaitingOperand.add(mOperand);
                break;
            case SUBTRACT:
                mOperand = mWaitingOperand.subtract(mOperand);
                break;
            case MULTIPLY:
                mOperand = mWaitingOperand.multiply(mOperand);
                break;
            case DIVIDE:
                if (mOperand.doubleValue() != 0) {
                    mOperand = mWaitingOperand.divide(mOperand, mathContext);
                }
                break;
        }
    }

    public void setOperand(BigDecimal operand) {
        mOperand = operand;
    }

    public String toString() {
        return mOperand.toString();
    }
}
