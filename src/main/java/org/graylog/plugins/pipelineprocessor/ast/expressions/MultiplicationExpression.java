/**
 * This file is part of Graylog Pipeline Processor.
 *
 * Graylog Pipeline Processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Pipeline Processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Pipeline Processor.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.pipelineprocessor.ast.expressions;

import org.antlr.v4.runtime.Token;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.MoreObjects.firstNonNull;

public class MultiplicationExpression extends BinaryExpression implements NumericExpression  {
    private final char operator;
    private AtomicReference<Class> type = new AtomicReference<>();

    public MultiplicationExpression(Token start, Expression left, Expression right, char operator) {
        super(start, left, right);
        this.operator = operator;
    }

    @Override
    public boolean isIntegral() {
        return getType().equals(Long.class);
    }

    @Override
    public long evaluateLong(EvaluationContext context) {
        return (Long) firstNonNull(evaluateUnsafe(context), 0);
    }

    @Override
    public double evaluateDouble(EvaluationContext context) {
        return (Double) firstNonNull(evaluateUnsafe(context), 0);
    }

    @Nullable
    @Override
    public Object evaluateUnsafe(EvaluationContext context) {
        final NumericExpression left = (NumericExpression) this.left;
        final NumericExpression right = (NumericExpression) this.right;

        if (isIntegral()) {
            final long l = left.evaluateLong(context);
            final long r = right.evaluateLong(context);

            switch (operator) {
                case '*':
                    return l * r;
                case '/':
                    return l / r;
                case '%':
                    return l % r;
                default:
                    throw new IllegalStateException("Invalid operator, this is a bug.");
            }
        } else {
            final double l = left.evaluateDouble(context);
            final double r = right.evaluateDouble(context);

            switch (operator) {
                case '*':
                    return l * r;
                case '/':
                    return l / r;
                case '%':
                    return l % r;
                default:
                    throw new IllegalStateException("Invalid operator, this is a bug.");
            }
        }
    }

    @Override
    public Class getType() {
        final Class theType = type.get();
        if (theType != null) {
            return theType;
        }
        final NumericExpression left = (NumericExpression) this.left;
        final NumericExpression right = (NumericExpression) this.right;

        // double + double = double, long + long = long, the other cases are caught by the type checker
        if (left.isIntegral()) {
            if (right.isIntegral()) {
                type.set(Long.class);
            } else {
                type.set(Double.class);
            }
        } else {
            type.set(Double.class);
        }

        return type.get();
    }


    @Override
    public String toString() {
        return left.toString() + " " + operator + " " + right.toString();
    }

}
