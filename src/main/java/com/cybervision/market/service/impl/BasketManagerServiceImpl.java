package com.cybervision.market.service.impl;

import com.cybervision.market.dao.DaoInterface;
import com.cybervision.market.dao.impl.*;
import com.cybervision.market.entity.*;
import com.cybervision.market.exception.ItemNotFoundException;
import com.cybervision.market.exception.NotEnoughMoneyException;
import com.cybervision.market.service.BasketManagerService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service("basketManagerService")
public class BasketManagerServiceImpl implements BasketManagerService {

    private DaoInterface<User> userDao = new UserDaoImpl();
    private DaoInterface<Vendor> vendorDao = new VendorDaoImpl();
    private DaoInterface<Basket> basketDao = new BasketDaoImpl();
    private ItemDaoImpl itemDao = new ItemDaoImpl();
    private DaoInterface<Supply> supplyDao = new SupplyDaoImpl();


    @Override
    public Long saveItem(Item item) {
        return itemDao.save(item);
    }

    @Override
    public void updateItem(Item item) throws NotEnoughMoneyException {
        itemDao.update(item);
    }


    @Override
    public Long saveBasket(Basket basket) throws NotEnoughMoneyException {
        return basketDao.save(basket);
    }

    @Override
    public void updateBasket(Basket basket) throws NotEnoughMoneyException {
        basketDao.update(basket);
    }

    @Override
    public Set<Basket> getAllBasketsFromUser(User user) throws NotEnoughMoneyException, ItemNotFoundException {
        return userDao.get(user.getId()).getBaskets();
    }

    @Override
    public Set<Basket> getBasketsAmountFromUser(User user) throws NotEnoughMoneyException, ItemNotFoundException {
        return null;
    }

    @Override
    public void saveSupply(Supply supply) {
        supplyDao.save(supply);
    }

    @Override
    public void updateSupply(Supply supply) throws NotEnoughMoneyException {
        supplyDao.update(supply);
    }

    @Override
    public Set<Supply> getAllSuppliesFromVendor(Vendor vendor) {
        return vendorDao.get(vendor.getId()).getSupplies();
    }

    @Override
    public Set<Basket> getSuppliesAmountFromUser(User user) throws NotEnoughMoneyException, ItemNotFoundException {
        return null;
    }


    @Override
    public void addItemByIdToBasket(Long id, Basket basket, int quantity) throws NotEnoughMoneyException, ItemNotFoundException {
        Item item = itemDao.get(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }
        Optional<ItemOrder> existedItemOrder = basket.getItemOrders().stream().filter(o -> item.equals(o.getItem())).findFirst();
        if (existedItemOrder.isPresent()) {
            basket.getItemOrders().remove(existedItemOrder.get());
            existedItemOrder.get().setQuantity(existedItemOrder.get().getQuantity() + quantity);
            itemDao.updateItemOrder(existedItemOrder.get());
            basket.getItemOrders().add(existedItemOrder.get());
        } else {
            ItemOrder itemOrder = new ItemOrder();
            itemOrder.setBasket(basket);
            itemOrder.setItem(item);
            itemOrder.setQuantity(quantity);
            itemDao.saveItemOrder(itemOrder);
            basket.getItemOrders().add(itemOrder);
        }
    }

    @Override
    public void addItemByIdToSupply(long id, Supply supply, int quantity) {
        Item item = itemDao.get(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }
        Optional<SupplyList> existedSupplyList = supply.getSupplyList().stream().filter(o -> item.equals(o.getItem())).findFirst();
        if (existedSupplyList.isPresent()) {
            supply.getSupplyList().remove(existedSupplyList.get());
            existedSupplyList.get().setQuantity(existedSupplyList.get().getQuantity() + quantity);
            itemDao.updateSupplyList(existedSupplyList.get());
            supply.getSupplyList().add(existedSupplyList.get());
        } else {
            SupplyList supplyList = new SupplyList();
            supplyList.setSupply(supply);
            supplyList.setItem(item);
            supplyList.setQuantity(quantity);
            itemDao.saveSupplyList(supplyList);
            supply.getSupplyList().add(supplyList);
        }
    }

    @Override
    public Long saveVendor(Vendor vendor) {
        return vendorDao.save(vendor);
    }

    @Override
    public Vendor getVendor(Long id) {
        return vendorDao.get(id);
    }

    @Override
    public void saveUser(User user) {
        userDao.save(user);
    }

    @Override
    public void deleteUser(User user) {
        userDao.delete(user);
    }

    @Override
    public User getUser(Long id) {
        return userDao.get(id);
    }

    @Override
    public void buyBasket(Basket basket) throws NotEnoughMoneyException {
        if (basket.getUser().getAvailableMoney() >= basket.calculateSummaryCost()) {
            basket.setBought(true);
            basketDao.update(basket);
        } else {
            throw new NotEnoughMoneyException(basket.getUser().getAvailableMoney(), basket.calculateSummaryCost());
        }
    }

    @Override
    public Set<Item> getAllItemsFromBasket(Basket basket) throws NotEnoughMoneyException, ItemNotFoundException {
        return basketDao.get(basket.getId()).getItems();
    }

    @Override
    public Double calculateSummaryCost(Basket basket) {
        Basket tmpBasket = basketDao.get(basket.getId());
        return tmpBasket.calculateSummaryCost();
    }

    @Override
    public Basket getBasket(Long id) {
        return basketDao.get(id);
    }

    @Override
    public void clearDatabase() {
        itemDao.removeAll();
        supplyDao.removeAll();
        basketDao.removeAll();
        vendorDao.removeAll();
        userDao.removeAll();
    }

}
