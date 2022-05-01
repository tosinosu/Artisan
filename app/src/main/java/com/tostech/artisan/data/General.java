package com.tostech.artisan.data;

public class General {
    private BusinessListData listItems;
    private RatingData rateItems;

    public General(BusinessListData listItems, RatingData rateItems) {
        this.listItems = listItems;
        this.rateItems = rateItems;
    }

    public BusinessListData getListItems() {
        return listItems;
    }

    public RatingData getRateItems() {
        return rateItems;
    }
}
