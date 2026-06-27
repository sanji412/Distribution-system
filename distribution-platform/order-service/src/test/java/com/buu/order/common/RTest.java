package com.buu.order.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    void successReturnsTeacherStyleResponseShape() {
        R<String> response = R.success("ok");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMsg()).isEqualTo("操作成功");
        assertThat(response.getData()).isEqualTo("ok");
    }
}
