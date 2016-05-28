package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.location.Location;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

/**
 * The {@link Location} implementation backed by an ANTLR {@link ParserRuleContext}.
 *
 * @author Eldar Abusalimov
 */
public class RuleLocation extends AbstractLocation {
    private final ParserRuleContext ruleContext;

    public RuleLocation(ParserRuleContext ruleContext) {
        this.ruleContext = Objects.requireNonNull(ruleContext);
    }

    public ParserRuleContext getRuleContext() {
        return ruleContext;
    }

    @Override
    public Token getStartToken() {
        return ruleContext.getStart();
    }

    @Override
    public Token getStopToken() {
        return ruleContext.getStop();
    }
}
