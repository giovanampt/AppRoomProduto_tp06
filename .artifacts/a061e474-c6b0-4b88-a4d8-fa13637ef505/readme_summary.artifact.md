# Resumo do Projeto AppRoomProduto - Complemento Prático (Parte A)

Este documento contém o resumo de todas as implementações realizadas na Parte A do projeto **AppRoomProduto** (desenvolvido com Jetpack Room, ViewBinding e Corrotinas), organizadas item por item para inclusão no `README.md`.

---

## 1. Limpar os campos após inserção
- **O que foi feito:** Após inserir um produto com sucesso no banco de dados (dentro do `lifecycleScope.launch`), limpamos os campos de entrada (`editProductName`, `editProductPrice`, `editProductDescription`, `editProductQuantity`) usando `.text.clear()` e retornamos o foco para o campo Nome com `.requestFocus()`.
- **Trecho de código principal:**
```kotlin
binding.editProductName.text.clear()
binding.editProductPrice.text.clear()
binding.editProductDescription.text.clear()
binding.editProductQuantity.text.clear()
binding.editProductName.requestFocus()
```

---

## 2. Validação de preço
- **O que foi feito:** Adicionamos uma validação antes de inserir o produto. Caso o preço esteja vazio, seja zero ou negativo, o app impede o salvamento e exibe uma mensagem de erro diretamente no campo de preço.
- **Trecho de código principal:**
```kotlin
val price = priceText.toDoubleOrNull() ?: 0.0
if (priceText.isEmpty() || price <= 0.0) {
    binding.editProductPrice.error = "Informe um preço válido"
    return@setOnClickListener
}
```

---

## 3. Lista na tela (`TextView` / `ScrollView`)
- **O que foi feito:** Adicionamos um componente `ScrollView` contendo um `TextView` (`textProductList`) no layout da `MainActivity` abaixo dos botões. Ao clicar em "Listar Produtos", a lista recuperada do banco de dados é formatada e exibida na tela.
- **Trecho de código principal:**
```kotlin
val sb = StringBuilder()
products.forEach {
    sb.append("ID: ${it.id}\nNome: ${it.name}\nPreço: R$${it.price}\n...")
}
binding.textProductList.text = sb.toString()
```

---

## 4. Total do estoque
- **O que foi feito:** Calculamos a quantidade de produtos cadastrados (`products.size`) e a soma total dos preços (`products.sumOf { it.price }`), exibindo essas informações no topo da listagem na tela.
- **Trecho de código principal:**
```kotlin
val totalCount = products.size
val totalPrice = products.sumOf { it.price }
sb.append("Quantidade cadastrados: $totalCount | Soma preços: R$$totalPrice\n\n")
```

---

## 5. Buscar por nome
- **O que foi feito:**
  1. Criamos a consulta no `ProductDao`: `@Query("SELECT * FROM product WHERE name LIKE :nome") suspend fun buscarPorNome(nome: String): List<Product>`.
  2. Adicionamos um campo de busca (`editSearchName`) e um botão "Buscar Produto" no layout.
  3. Na `MainActivity`, filtramos os produtos usando corrotinas e exibimos os resultados na tela.
- **Trecho de código principal:**
```kotlin
// DAO
@Query("SELECT * FROM product WHERE name LIKE :nome")
suspend fun buscarPorNome(nome: String): List<Product>

// Activity
val query = binding.editSearchName.text.toString()
val products = productDao.buscarPorNome("%$query%")
```

---

## 6. Excluir produtos
- **O que foi feito:**
  1. Criamos a consulta no `ProductDao`: `@Query("DELETE FROM product") suspend fun deleteAll()`.
  2. Adicionamos o botão "Excluir Todos" (`btnExcluirTodos`) na interface.
  3. Programamos a ação na `MainActivity` para limpar todos os registros da tabela e atualizar o `TextView`.
- **Trecho de código principal:**
```kotlin
// DAO
@Query("DELETE FROM product")
suspend fun deleteAll()

// Activity
binding.btnExcluirTodos.setOnClickListener {
    lifecycleScope.launch {
        productDao.deleteAll()
        binding.textProductList.text = "Todos os produtos foram excluídos."
    }
}
```

---

## 7. Atualizar produto
- **O que foi feito:**
  1. Adicionamos `@Update` e um método `@Query("SELECT * FROM product WHERE id = :id") getById(id: Int)` no `ProductDao`.
  2. Adicionamos campos para informar o ID e o novo preço no layout (`editUpdateId`, `editUpdatePrice`) e um botão "Atualizar Preço".
  3. Na `MainActivity`, localizamos o produto pelo ID, atualizamos o preço e salvamos no banco.
- **Trecho de código principal:**
```kotlin
// DAO
@Update
suspend fun updateProduct(product: Product)

// Activity
val p = productDao.getById(id)
if (p != null) {
    val updated = p.copy(price = newPrice)
    productDao.updateProduct(updated)
}
```

---

## 8. Novo campo na tabela (`quantity`) e migração do banco
- **O que foi feito:**
  1. Adicionamos o atributo `quantity: Int` na data class `Product`.
  2. Adicionamos o campo `editProductQuantity` no layout e atualizamos a leitura e exibição na `MainActivity`.
  3. Incrementamos a versão do banco de `1` para `2` na anotação `@Database(entities = [Product::class], version = 2)`.
- **O que aconteceu com os produtos já cadastrados e por quê?**
  - Como utilizamos `.fallbackToDestructiveMigration()` na construção do banco de dados, ao alterar a versão do esquema de `1` para `2` sem fornecer uma migração customizada (`Migration`), o Room destruiu as tabelas antigas e recriou o banco do zero. Por esse motivo, os dados cadastrados anteriormente foram apagados. Para evitar perda de dados em produção, seria necessário implementar uma `Migration` explicitando como migrar os dados da v1 para a v2.
