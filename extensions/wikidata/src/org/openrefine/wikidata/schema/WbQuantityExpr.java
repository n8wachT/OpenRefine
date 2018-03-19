/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2018 Antonin Delpeuch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.openrefine.wikidata.schema;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;
import org.openrefine.wikidata.schema.exceptions.SkipSchemaExpressionException;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WbQuantityExpr implements WbExpression<QuantityValue> {

    private final WbExpression<? extends StringValue> amountExpr;
    private final WbExpression<? extends ItemIdValue> unitExpr;

    /**
     * Creates an expression for a quantity, which contains two sub-expressions: one
     * for the amount (a string with a particular format) and one for the unit,
     * which is optional.
     * 
     * Setting unitExpr to null will give quantities without units. Setting it to a
     * non-null value will make the unit mandatory: if the unit expression fails to
     * evaluate, the whole quantity expression will fail too.
     */
    @JsonCreator
    public WbQuantityExpr(@JsonProperty("amount") WbExpression<? extends StringValue> amountExpr,
            @JsonProperty("unit") WbExpression<? extends ItemIdValue> unitExpr) {
        Validate.notNull(amountExpr);
        this.amountExpr = amountExpr;
        this.unitExpr = unitExpr;
    }

    @Override
    public QuantityValue evaluate(ExpressionContext ctxt)
            throws SkipSchemaExpressionException {
        StringValue amount = getLanguageExpr().evaluate(ctxt);
        // we know the amount is nonnull, nonempty here

        BigDecimal parsedAmount = null;
        try {
            parsedAmount = new BigDecimal(amount.getString());
        } catch (NumberFormatException e) {
            throw new SkipSchemaExpressionException();
        }

        if (getUnitExpr() != null) {
            ItemIdValue unit = getUnitExpr().evaluate(ctxt);
            return Datamodel.makeQuantityValue(parsedAmount, unit.getIri());
        }

        return Datamodel.makeQuantityValue(parsedAmount);
    }

    @JsonProperty("amount")
    public WbExpression<? extends StringValue> getLanguageExpr() {
        return amountExpr;
    }

    @JsonProperty("unit")
    public WbExpression<? extends ItemIdValue> getUnitExpr() {
        return unitExpr;
    }
}