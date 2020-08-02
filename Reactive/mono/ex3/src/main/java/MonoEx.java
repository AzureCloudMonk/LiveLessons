import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform Mono operations, including
 * fromCallable(), subscribeOn(), zipWith(), doOnSuccess(), then(),
 * and the parallel thread pool.
 */
public class MonoEx {
    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Mono<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m1 = makeBigFraction(random);

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m2 = makeBigFraction(random);
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString()
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return m1
            // Add results after m1 and m2 both complete.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#zipWith-reactor.core.publisher.Mono-java.util.function.BiFunction-
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty mono to synchronize with the
            // AsyncTester.
            .then();
    }

    /**
     * A factory method that creates a random big fraction and
     * subscribes it to be reduced and multiplied in a thread pool.
     */
    private static Mono<BigFraction> makeBigFraction(Random random) {
        return Mono
            // Factory method that makes a random big fraction and
            // multiplies it with a constant.
            .just(BigFractionUtils.makeBigFraction(random, true)
                    .multiply(sBigReducedFraction))

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());
    }
}