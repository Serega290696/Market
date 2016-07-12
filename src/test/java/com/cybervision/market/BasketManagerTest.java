package com.cybervision.market;

import com.cybervision.market.BootApplication;
import com.cybervision.market.dao.impl.UserDaoImpl;
import com.cybervision.market.entity.*;
import com.cybervision.market.exception.ItemNotFoundException;
import com.cybervision.market.exception.NotEnoughMoneyException;
import com.cybervision.market.service.BasketManagerService;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = OnlineMarketApplication.class)
public class BasketManagerTest extends Assert {

    /*

    todo Один ДАО - ОДНА независимая (не таблицы для связи многие-ко-многим) сущность!
    todo DaoGeneric by Spring
    todo @Transaction
    todo Удалить повторяющийся код! Особенно с транзакциями
    todo


     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private Item item5;
    private Item item6;
    private Item item7;
    private Basket basket;
    private Supply supply;
    private Supply supply2;
    private Vendor vendor;
    private Vendor vendor2;
    @Autowired
    private BasketManagerService basketManagerService;

    @Before
    public void setUp() throws Exception {
        User user = new User("beltser@gmail.com", "qweqwe", 500d);
        item1 = new Item("pen", 20d);
        item2 = new Item("pencil", 20d);
        item3 = new Item("ball", 20d);
        item4 = new Item("dol", 20d);
        item5 = new Item("apple", 20d);
        item6 = new Item("toy car", 200d);
        item7 = new Item("toy airplane", 300d);
        basket = new Basket("The first basket", user, new Timestamp(new Date().getTime()));
        supply = new Supply(vendor, Timestamp.valueOf(LocalDateTime.now()));
        supply2 = new Supply(vendor, Timestamp.valueOf(LocalDateTime.now()));
        vendor = new Vendor("Roshen", "verhovna rada");
        vendor2 = new Vendor("Prosokvash", "selo Prosokvasheno");
//        basketManagerService = new BasketManagerServiceImpl();
        basketManagerService.clearDatabase();
    }

    public BasketManagerTest() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    }

    @Test
    public void testSupplyFlow() throws Exception {
        long ids[] = new long[5];
        ids[0] = basketManagerService.saveItem(item1);
        ids[1] = basketManagerService.saveItem(item2);
        ids[2] = basketManagerService.saveItem(item3);
        ids[3] = basketManagerService.saveItem(item4);
        ids[4] = basketManagerService.saveItem(item5);
        Long idVendor1 = basketManagerService.saveVendor(vendor);
        vendor.setId(idVendor1);
        basketManagerService.saveVendor(vendor2);
        basketManagerService.saveSupply(supply);
        basketManagerService.saveSupply(supply2);
        basketManagerService.addItemByIdToSupply(ids[0], supply, 2);
        basketManagerService.addItemByIdToSupply(ids[1], supply, 2);
        basketManagerService.addItemByIdToSupply(ids[3], supply, 2);

        basketManagerService.addItemByIdToSupply(ids[0], supply2, 2);
        basketManagerService.addItemByIdToSupply(ids[1], supply2, 2);
        basketManagerService.addItemByIdToSupply(ids[2], supply2, 2);

        int wrongItemId = -2910212;
        expectedException.expect(ItemNotFoundException.class);
        basketManagerService.addItemByIdToSupply(wrongItemId, supply, 2);

    }

    @Test
    public void testSaveBasketCostLimitExceed() throws Exception {
        Long id = basketManagerService.saveItem(item1);
        Long id2 = basketManagerService.saveItem(item2);
        Long id6 = basketManagerService.saveItem(item6);
        Long id7 = basketManagerService.saveItem(item7);
        basketManagerService.saveBasket(basket);
        basketManagerService.addItemByIdToBasket(id, basket, 2);
        basketManagerService.addItemByIdToBasket(id, basket, 5);
        basketManagerService.addItemByIdToBasket(id2, basket, 1);
        basketManagerService.addItemByIdToBasket(id6, basket, 1);
        basket = basketManagerService.getBasket(basket.getId());
        basketManagerService.buyBasket(basket);
        basketManagerService.addItemByIdToBasket(id7, basket, 1);
        basket = basketManagerService.getBasket(basket.getId());
        expectedException.expect(NotEnoughMoneyException.class);
        basketManagerService.buyBasket(basket);
    }

    @Test
    public void testCalculateSummaryCost() throws Exception {
        long ids[] = new long[5];
        ids[0] = basketManagerService.saveItem(item1);
        ids[1] = basketManagerService.saveItem(item2);
        ids[2] = basketManagerService.saveItem(item3);
        ids[3] = basketManagerService.saveItem(item4);
        ids[4] = basketManagerService.saveItem(item5);
        basketManagerService.saveBasket(basket);
        for (long id : ids) {
            basketManagerService.addItemByIdToBasket(id, basket, 2);
        }
        Double basketCost = basketManagerService.calculateSummaryCost(basket);

        Double correctResult = 2 * (item1.getCost() + item2.getCost() + item3.getCost() + item4.getCost() + item5.getCost());
        assertEquals(basketCost, correctResult);
    }

    @Test
    public void testAddItemToBasketTwice() throws Exception {
        basketManagerService.saveItem(item2);
        Long id = basketManagerService.saveItem(item1);
        Long id3 = basketManagerService.saveItem(item3);
        basketManagerService.saveBasket(basket);
        basketManagerService.addItemByIdToBasket(id, basket, 2);

        basket = basketManagerService.getBasket(basket.getId());
        assertNotNull(basket);

        basketManagerService.addItemByIdToBasket(id3, basket, 3);
        basketManagerService.addItemByIdToBasket(id, basket, 4);

        basket = basketManagerService.getBasket(this.basket.getId());
        assertEquals(2, basket.getItemOrders().size());
        int itemsQuantity = basket.getItemOrders().stream().reduce(
                0,
                (s, i) -> s + i.getQuantity(),
                (s1, s2) -> s1 + s2
        );
        assertEquals(9, itemsQuantity);
    }

    @Ignore
    @Test
    public void testGetUserByEmail() throws Exception {
        UserDaoImpl userDao = new UserDaoImpl();
        final String email = "serg29@gmail.com";
        basketManagerService.saveUser(new User(email, "lalala", 200d));
        User userByEmail = userDao.getByEmail("serg29@gmail.com");
        System.out.println(userByEmail);
        assertNotNull(userByEmail);
    }

    @Ignore
    @Test
    public void testUpdateBasket() throws Exception {
        basketManagerService.saveBasket(basket);
        basket.setTitle("CHANGED TITLE 2");
        basketManagerService.updateBasket(basket);
        Basket basket2 = basketManagerService.getBasket(this.basket.getId());
        assertEquals(basket2.getTitle(), basket.getTitle());
    }

    @Ignore
    @Test
    public void testAddItemByIdToBasket() throws Exception {
        Long itemId1 = basketManagerService.saveItem(item1);
        Long itemId2 = basketManagerService.saveItem(item2);
        Long itemId3 = basketManagerService.saveItem(item3);
        basketManagerService.saveBasket(basket);
        basketManagerService.addItemByIdToBasket(itemId1, basket, 1);
        basketManagerService.addItemByIdToBasket(itemId2, basket, 1);
        basketManagerService.addItemByIdToBasket(itemId3, basket, 1);
        basketManagerService.saveBasket(basket);
    }

    @Ignore
    @Test
    public void testAddItemByWrongIdToBasket() throws Exception {
        Long itemId1 = basketManagerService.saveItem(item1);
        Random random = new Random();
        Long wrongId = 10_000 + random.nextLong();
        basketManagerService.addItemByIdToBasket(itemId1, basket, 1);
        expectedException.expect(ItemNotFoundException.class);
        basketManagerService.addItemByIdToBasket(wrongId, basket, 1);
        basketManagerService.saveBasket(basket);
    }
}
