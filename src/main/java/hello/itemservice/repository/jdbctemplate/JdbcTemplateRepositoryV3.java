package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert
 * Insert 코드에서만 동작 가능.
 */
@Slf4j
public class JdbcTemplateRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateRepositoryV3(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id");
//                .usingColumns("item_name", "price", "quantity"); // 생략 가능.
    }

    @Override
    public Item save(Item item) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(params);
        item.setId(key.longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item" +
                " set item_name = :itemName, price= :price, quantity= :quantity" +
                " where id = :id";

        // PLUS : MapSqlParameterSource | Map 과 유사함. SQL 타입을 지정할 수 있는 등 SQL 에 더 특화됨.
        //                              | 메서드 체인 제공.
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, params);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = :id";

        try{
            // PLUS : 단순 Map 의 사용.
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        // PLUS : BeanPropertySqlParameterSource | 자바빈 프로퍼티 규약을 통해서 자동으로 파라미터 객체 생성.
        //                                       | 따로 cond 로 넘어오지 않는 값을 추가해야할 경우 사용하지 못함.
        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }
        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());
    }

    // 데이터베이스의 반환 결과인 ResultSet 을 객체로 반환.
    // PLUS : ResultSet 의 결과를 받아서 자바빈 규약에 맞추어 데이터를 반환함.
    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
        // camel 지원. EX) item_name -> itemName 자동 지원.
    }
}
