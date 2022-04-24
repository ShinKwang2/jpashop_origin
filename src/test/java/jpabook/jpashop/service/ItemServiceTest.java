package jpabook.jpashop.service;

import jpabook.jpashop.domain.Item;
import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    @Test
    void 아이템_등록() throws Exception {
        //given
        Item item = new Book();
        item.setName("그리스 로마 신화");

        //when
        Long savedId = itemService.saveItem(item);
        Item foundItem = itemService.findOne(savedId);

        //then
        assertThat(item).isEqualTo(foundItem);
     }

      @Test
      void 아이템_전체조회() throws Exception {
          //given
          Item album = new Album();
          Item movie = new Movie();
          album.setName("8집 앨범");
          movie.setName("놈놈놈");

          //when
          itemService.saveItem(album);
          itemService.saveItem(movie);
          List<Item> items = itemService.findItems();

          //then
          assertThat(items.size()).isEqualTo(2);
          assertThat(items).contains(album, movie);
       }
}