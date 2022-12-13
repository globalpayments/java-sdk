package com.global.api.entities;

import com.global.api.builders.PayFacBuilder;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.enums.UserType;
import com.global.api.entities.payFac.Person;
import com.global.api.entities.payFac.UserReference;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class User {
    // This is a label to identify the user
    private String name;
    // Global Payments time indicating when the object was created in ISO-8601 format
    private DateTime timeCreated;
    // The date and time the resource object was last changed
    private DateTime timeLastUpdated;
    private String email;
    private List<Address> addresses;
    private PhoneNumber contactPhone;
    // A further description of the status of merchant boarding
    private String statusDescription;
    // The result of the action executed
    private String responseCode;
    private UserReference userReference;
    private List<Person> personList;
    private List<PaymentMethodList> paymentMethods;

    /**
     * Creates an `User` object from an existing user ID
     */
    public static User fromId(String userId, UserType userType) {
        return
                new User()
                        .setUserReference(
                                new UserReference()
                                        .setUserId(userId)
                                        .setUserType(userType));
    }

    public PayFacBuilder<User> edit() {
        PayFacBuilder<User> builder =
                new PayFacBuilder<User>(TransactionType.Edit)
                        .withUserReference(this.userReference);

        if (userReference.getUserType() != null) {
            builder = builder.withModifier(TransactionModifier.valueOf(userReference.getUserType().getValue()));
        }

        return builder;
    }

}