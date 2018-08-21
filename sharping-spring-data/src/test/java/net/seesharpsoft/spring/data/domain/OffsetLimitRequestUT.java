package net.seesharpsoft.spring.data.domain;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.hamcrest.Matchers.equalTo;

public class OffsetLimitRequestUT {
    
    @Test
    public void constructor_should_create_correct_instance() {
        Sort sort = new Sort(Sort.Direction.DESC,"test");
        
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(545, 12, sort);
        
        MatcherAssert.assertThat(offsetLimitRequest.getOffset(), equalTo(545));
        MatcherAssert.assertThat(offsetLimitRequest.getPageSize(), equalTo(12));
        MatcherAssert.assertThat(offsetLimitRequest.getSort(), equalTo(sort));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_not_accept_incorrect_offset() {
        new OffsetLimitRequest(-12, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_not_accept_incorrect_limit_0() {
        new OffsetLimitRequest(0, -12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_not_accept_incorrect_limit_1() {
        new OffsetLimitRequest(0, 0);
    }

    @Test
    public void page_should_be_calculated_correctly_0() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(0, 12);

        MatcherAssert.assertThat(offsetLimitRequest.getPageNumber(), equalTo(0));
    }

    @Test
    public void page_should_be_calculated_correctly_1() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(5, 12);

        MatcherAssert.assertThat(offsetLimitRequest.getPageNumber(), equalTo(1));
    }

    @Test
    public void page_should_be_calculated_correctly_2() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(24, 12);

        MatcherAssert.assertThat(offsetLimitRequest.getPageNumber(), equalTo(2));
    }

    @Test
    public void page_should_be_calculated_correctly_3() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(25, 12);

        MatcherAssert.assertThat(offsetLimitRequest.getPageNumber(), equalTo(3));
    }

    @Test
    public void hasPrevious_should_return_true_if_so() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(25, 12);

        MatcherAssert.assertThat(offsetLimitRequest.hasPrevious(), equalTo(true));
    }

    @Test
    public void hasPrevious_should_return_false_if_so() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(0, 12);

        MatcherAssert.assertThat(offsetLimitRequest.hasPrevious(), equalTo(false));
    }
    
    @Test
    public void next_should_return_next_page() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(25, 12);

        Pageable next = offsetLimitRequest.next();
        MatcherAssert.assertThat(next.getPageNumber(), equalTo(offsetLimitRequest.getPageNumber() + 1));
        MatcherAssert.assertThat(next.getOffset(), equalTo(offsetLimitRequest.getOffset() + offsetLimitRequest.getPageSize()));
        MatcherAssert.assertThat(next.getPageSize(), equalTo(offsetLimitRequest.getPageSize()));
    }

    @Test
    public void previous_should_return_previous_page() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(25, 12);

        Pageable previous = offsetLimitRequest.previousOrFirst();
        MatcherAssert.assertThat(previous.getPageNumber(), equalTo(offsetLimitRequest.getPageNumber() - 1));
        MatcherAssert.assertThat(previous.getOffset(), equalTo(offsetLimitRequest.getOffset() - offsetLimitRequest.getPageSize()));
        MatcherAssert.assertThat(previous.getPageSize(), equalTo(offsetLimitRequest.getPageSize()));
    }

    @Test
    public void first_should_return_first_page() {
        OffsetLimitRequest offsetLimitRequest = new OffsetLimitRequest(25, 12);

        Pageable first = offsetLimitRequest.first();
        MatcherAssert.assertThat(first.getPageNumber(), equalTo(0));
        MatcherAssert.assertThat(first.getOffset(), equalTo(0));
        MatcherAssert.assertThat(first.getPageSize(), equalTo(1));
    }
}
