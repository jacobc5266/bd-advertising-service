package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();

        /* original implementation:
        boolean allTruePredicates = true;
        for (TargetingPredicate predicate : targetingPredicates) {
            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
            if (!predicateResult.isTrue()) {
                allTruePredicates = false;
                break;
            }
        }
         */

        /* Mastery Task 1 implementation

        boolean allTruePredicates = targetingPredicates.stream()
                .map(predicate -> predicate.evaluate(requestContext))
                .allMatch(TargetingPredicateResult::isTrue);

         */

        // Mastery Task 2 Implementation
        // Create ExecutorService
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<TargetingPredicateResult>> futures = new ArrayList<>();

        try {
            for (com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate predicate : targetingPredicates) {
                Future<TargetingPredicateResult> future = executorService.submit(() -> predicate.evaluate(requestContext));
                futures.add(future);
            }

            // Process Results
            for (Future<TargetingPredicateResult> future : futures) {
                try {
                    TargetingPredicateResult result = future.get();
                    if (!result.isTrue()) {
                        return TargetingPredicateResult.FALSE;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return TargetingPredicateResult.FALSE;
                }
            }
        } finally {
            executorService.shutdown();
        }


        return TargetingPredicateResult.TRUE;
    }
}
