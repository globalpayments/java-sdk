package com.global.api.network.entities.mpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class MPDLTable60 implements IMPDLTable {

    @Getter
    @Setter
    private Integer noOfProducts;
    @Getter
    @Setter
    private List<Products> products;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setNoOfProducts(sp.readInt(3));
        this.products = new ArrayList<>();

        for (int index = 0; index < this.getNoOfProducts(); index++) {
            Products product = new Products();
            product.setReceiptDescription(sp.readString(40));
            product.setConexxusCode(sp.readString(3));
            product.setNoOfCardTypes(sp.readInt(2));
            List<Cards> cards = new ArrayList<>();
            for (int cardIndex = 0; cardIndex < product.getNoOfCardTypes(); cardIndex++) {
                Cards card = new Cards();
                card.setHostCardType(sp.readInt(2));
                card.setProductCodeLength(sp.readInt(1));
                card.setProductCode(sp.readString(card.getProductCodeLength()));
                cards.add(card);
            }
            product.setCards(cards);
            products.add(product);
        }
        this.setProducts(products);
        return new MPDLTable(this);
    }

    @ToString
    public class Products {
        @Getter
        @Setter
        private String receiptDescription;
        @Getter
        @Setter
        private String conexxusCode;
        @Getter
        @Setter
        private Integer noOfCardTypes;
        @Getter
        @Setter
        private List<Cards> cards;
    }

    @ToString
    public class Cards {
        @Getter
        @Setter
        private Integer hostCardType;
        @Getter
        @Setter
        private Integer productCodeLength;
        @Getter
        @Setter
        private String productCode;
    }
}
