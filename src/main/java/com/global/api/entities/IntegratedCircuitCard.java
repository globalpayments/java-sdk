package com.global.api.entities;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntegratedCircuitCard {
    private String dedicated_file_name;
    private String application_label;
    private String application_expiry_date;
    private String application_effective_date;
    private String application_interchange_profile;
    private String application_version_number;
    private String application_transaction_counter;
    private String application_usage_control;
    private String application_preferred_name;
    private String application_display_name;
    private String application_pan_sequence_number;
    private String application_cryptogram;
    private String application_cryptogram_type;
    private String cardholder_verification_method_results;
    private String issuer_application_data;
    private String terminal_verification_results;
    private String unpredictable_number;
    private String pos_entry_mode;
    private String terminal_type;
    private String ifd_serial_number;
    private String terminal_country_code;
    private String terminal_identification;
    private String tac_default;
    private String tac_denial;
    private String tac_online;
    private String transaction_type;
    private String transaction_currency_code;
    private String transaction_status_information;
    private String cryptogram_information_data;
    private String pin_statement;
    private String cvm_method;
    private String iac_default;
    private String iac_denial;
    private String iac_online;
    private String authorization_response_code;
}
