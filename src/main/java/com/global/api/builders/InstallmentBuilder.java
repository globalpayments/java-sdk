package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.IInstallmentEntity;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IInstallmentService;
import com.global.api.paymentMethods.Installment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class InstallmentBuilder extends BaseBuilder<Installment> {

    /**
     * Represents the Installment entity
     */
    private IInstallmentEntity entity;

    /**
     * Represents the parameterized constructor to set the installment Entity value
     *
     * @param entity
     */
    public InstallmentBuilder(IInstallmentEntity entity) {

        if (entity != null) {
            this.entity = entity;
        }
    }

    /**
     * Executes the Installment builder against the gateway.
     *
     * @param configName
     * @return {@link Installment}
     * @throws ApiException
     */
    public Installment execute(String configName) throws ApiException {
        super.execute(configName);
        IInstallmentService installmentClient = ServicesContainer.getInstance().getInstallmentClient(configName);
        return installmentClient.processInstallment(this);
    }
}
