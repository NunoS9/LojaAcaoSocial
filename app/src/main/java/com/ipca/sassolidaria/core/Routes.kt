package com.ipca.sassolidaria.core

object Routes {

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER_PASSWORD = "recover_password"

    const val HOME_COLABORADOR = "home_colaborador"
    const val HOME_BENEFICIARIO = "home_beneficiario"
    const val HOME_UTILIZADOR = "home_utilizador"

    // Colaborador

    const val PRODUTOS_LIST = "produtos_list"
    const val PRODUTO_CREATE = "produto_create"
    const val PRODUTO_EDIT = "produto_edit/{id}"
    const val DOACOES_LIST = "doacoes_list"
    const val CANDIDATURAS_LIST = "candidaturas_list"
    const val PERFIL_COLABORADOR = "perfil_colaborador"
    const val DOACAO_CREATE = "doacao_create"
    const val DOACAO_DETAIL = "doacao_detail"
    const val BENEFICIARIOS_LIST = "beneficiarios_list"

    // Beneficiário
    const val PEDIDOS_LIST = "pedidos_list"
    const val BENEFICIARIO_DETAIL = "beneficiario_detail"

    const val PEDIDOS_CREATE="pedidos_create"
    const val ENTREGAS_LIST = "entregas_list"
    const val MARCACOES_LIST = "marcacoes_list"
    const val PERFIL_BENEFICIARIO = "perfil_beneficiario"
    const val PEDIDO_DETAILS_BENEFICIARIO = "pedido_details_beneficiario"



    // Utilizador
    const val PERFIL_UTILIZADOR = "perfil_utilizador"
    const val NOTIFICACOES_UTILIZADOR = "notificacoes_utilizador"
    const val CANDIDATURA_UTILIZADOR = "candidatura_utilizador"
    const val CANDIDATURA_CREATE = "candidatura_create"
    const val CANDIDATURA_DETAIL_COLABORADOR = "candidatura_detail_colaborador"
}
