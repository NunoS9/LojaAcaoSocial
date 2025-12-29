# Loja de Ação Social do IPCA

Aplicação móvel Android desenvolvida no âmbito das Unidades Curriculares de **Projeto Aplicado** e **Programação de Dispositivos Móveis**, com o objetivo de digitalizar e otimizar a gestão dos apoios sociais dos Serviços de Ação Social do IPCA (SAS IPCA).

## 📱 Tecnologias Utilizadas

- **Android Studio**
- **Kotlin**
- **Jetpack Compose**
- **Firebase**
  - Firebase Authentication;
  - Firestore;
  - Firebase Cloud Messaging;
  - Firebase Cloud Functions.

## 🎯 Objetivo do Projeto

A aplicação pretende substituir processos manuais por uma solução digital que permita:

- Gestão eficiente de beneficiários;
- Controlo de inventário e stock;
- Registo e gestão de doações;
- Candidaturas a apoios sociais;
- Agendamento e acompanhamento de entregas;
- Notificações automáticas em tempo real.

## 👥 Perfis de Utilizador

- **Beneficiário**
  - Submeter candidaturas;
  - Pedir produtos;
  - Consultar histórico;
  - Receber notificações.

- **Colaborador SAS**
  - Gerir beneficiários;
  - Gerir produtos e stock;
  - Registar doações;
  - Processar entregas;
  - Enviar notificações.

> O perfil **Doador** é externo à aplicação e interage através do Colaborador SAS.

## 🔐 Autenticação e Segurança

- Autenticação feita com **Firebase Authentication**;
- Perfis de utilizador distintos;
- Regras de segurança no **Cloud Firestore**;
- Cumprimento do **RGPD**;
- Dados sensíveis protegidos.

## 🗄️ Estrutura de Dados

Os dados são armazenados no **Cloud Firestore**, incluindo:
- Utilizadores;
- Beneficiários;
- Produtos;
- Doações;
- Pedidos;
- Entregas;
- Notificações;
- Candidaturas.

Os documentos anexados às candidaturas são armazenados no **Firebase Cloud Storage**.

## 🔔 Notificações

Utiliza **Firebase Cloud Messaging** para:
- Alertas de entregas;
- Notificações de pedidos;
- Avisos de stock baixo;
- Alertas de validade de produtos;
- Mensagens de suporte.

## ⚙️ Backend

Não existe servidor tradicional. 
Toda a lógica de negócio é implementada com **Firebase Cloud Functions**, incluindo:
- Atualização automática de stock;
- Validação de dados;
- Envio de notificações;
- Processamento de doações;
- Gestão de estados de entregas.

## ▶️ Como Executar o Projeto

1. Abrir o projeto no **Android Studio**;
2. Garantir que tens um projeto configurado no **Firebase**;
3. Adicionar o ficheiro `google-services.json` à pasta `/app`;
4. Sincronizar o Gradle;
5. Executar a aplicação num emulador ou dispositivo físico.

## 📌 Requisitos

- Ligação à Internet;
- Conta Firebase configurada.

## 👨‍💻 Autores

- Nuno Miguel Rodrigues da Silva, a25417
- Tiago André Alves Castro, a25434  
- Hugo Francisco Pereira Azevedo, a25431 
- Gonçalo Edi Pinto Homem de Figueiredo, a26747 

## 🏫 Instituição

Instituto Politécnico do Cávado e do Ave  
Escola Superior de Tecnologia
