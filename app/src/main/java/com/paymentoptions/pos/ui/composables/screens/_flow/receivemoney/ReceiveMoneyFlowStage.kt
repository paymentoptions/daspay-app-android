package com.paymentoptions.pos.ui.composables.screens._flow.receivemoney

enum class ReceiveMoneyFlowStage {
    INPUT_MONEY, CHARGE_MONEY, TRANSACTION_FAILED, TRANSACTION_SUCCESSFUL, DIGITAL_SIGNATURE, RECEIPT,
}