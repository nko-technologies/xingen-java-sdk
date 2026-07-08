package de.xingen.sdk.paging;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/**
 * Lazily iterates every element across all pages of a paginated endpoint, fetching the next page
 * only once the current one is exhausted, so callers can {@code for}-loop a whole result set
 * without managing page indices or loading everything into memory upfront.
 */
public final class PageIterator<T> implements Iterable<T> {

    private final IntFunction<Page<T>> pageFetcher;

    public PageIterator(IntFunction<Page<T>> pageFetcher) {
        this.pageFetcher = pageFetcher;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Page<T> currentPage = pageFetcher.apply(0);
            private int indexInPage = 0;

            @Override
            public boolean hasNext() {
                advanceToNextNonExhaustedPage();
                return indexInPage < currentPage.getContent().size();
            }

            @Override
            public T next() {
                advanceToNextNonExhaustedPage();
                List<T> content = currentPage.getContent();
                if (indexInPage >= content.size()) {
                    throw new NoSuchElementException();
                }
                return content.get(indexInPage++);
            }

            private void advanceToNextNonExhaustedPage() {
                while (indexInPage >= currentPage.getContent().size() && !currentPage.isLast()) {
                    currentPage = pageFetcher.apply(currentPage.getNumber() + 1);
                    indexInPage = 0;
                }
            }
        };
    }
}
