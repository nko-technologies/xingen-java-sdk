package de.xingen.sdk.invoices;

import java.time.Duration;

/** Seam over {@link Thread#sleep} so poll-loop tests don't have to wait in real time. */
interface Sleeper {

    Sleeper REAL = duration -> Thread.sleep(duration.toMillis());

    void sleep(Duration duration) throws InterruptedException;
}
