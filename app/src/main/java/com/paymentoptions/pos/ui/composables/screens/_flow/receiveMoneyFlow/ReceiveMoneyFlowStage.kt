package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow

enum class ReceiveMoneyFlowStage {
    INPUT_MONEY, CHARGE_MONEY, TRANSACTION_FAILED, TRANSACTION_SUCCESSFUL, DIGITAL_SIGNATURE, RECEIPT,
}