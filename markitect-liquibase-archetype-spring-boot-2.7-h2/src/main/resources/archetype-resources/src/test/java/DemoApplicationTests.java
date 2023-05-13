package ${package};

import static ${package}.jooq.Tables.USER;
import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class DemoApplicationTests {

  @Autowired DSLContext create;

  @Test
  void contextLoads() {}

  @Test
  @Transactional
  void crudExecutes() {
    // given
    assertThat(create.fetchCount(USER)).isEqualTo(0);

    // when
    create
        .insertInto(USER)
        .columns(USER.USERNAME, USER.DISPLAYNAME)
        .values("kudou.shinichi", "工藤新一")
        .values("mouri.ran", "毛利蘭")
        .execute();

    // then
    assertThat(create.select(USER.USERNAME, USER.DISPLAYNAME).from(USER).fetch(USER.USERNAME))
        .containsExactlyInAnyOrder("kudou.shinichi", "mouri.ran");
    assertThat(
            create
                .select(USER.DISPLAYNAME)
                .from(USER)
                .where(USER.USERNAME.eq("kudou.shinichi"))
                .fetchOne(USER.DISPLAYNAME))
        .isEqualTo("工藤新一");

    // when
    create
        .update(USER)
        .set(USER.DISPLAYNAME, "江戸川コナン")
        .where(USER.USERNAME.eq("kudou.shinichi"))
        .execute();

    // then
    assertThat(
            create
                .select(USER.DISPLAYNAME)
                .from(USER)
                .where(USER.USERNAME.eq("kudou.shinichi"))
                .fetchOne(USER.DISPLAYNAME))
        .isEqualTo("江戸川コナン");

    // when
    create.deleteFrom(USER).where(USER.USERNAME.eq("kudou.shinichi")).execute();

    // then
    assertThat(create.select(USER.USERNAME, USER.DISPLAYNAME).from(USER).fetch(USER.USERNAME))
        .containsExactly("mouri.ran");
  }
}
