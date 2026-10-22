package br.unisanta.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.unisanta.approomproduto.dao.ProductDao
import br.unisanta.approomproduto.databinding.ActivityMainBinding
import br.unisanta.approomproduto.db.AppDatabase
import br.unisanta.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa o ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o banco de dados Room de forma recomendada
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration()
            .build()
        productDao = db.productDao()

        // Exemplo de inserção de produto
        binding.btnInserirProduto.setOnClickListener {
            val name = binding.editProductName.text.toString()
            val priceText = binding.editProductPrice.text.toString()
            val description = binding.editProductDescription.text.toString()
            val quantityText = binding.editProductQuantity.text.toString()

            if (name.isEmpty()) {
                binding.editProductName.error = "Preencha o nome"
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull() ?: 0.0
            if (priceText.isEmpty() || price <= 0.0) {
                binding.editProductPrice.error = "Informe um preço válido"
                return@setOnClickListener
            }

            val quantity = quantityText.toIntOrNull() ?: 0

            val product = Product(name = name, price = price, description = description, quantity = quantity)

            // Insere o produto no banco de dados de forma assíncrona (usando corrotina)
            lifecycleScope.launch {
                productDao.insertAll(product)
                Log.i("TAG", "Produto inserido: $name - R$$price - Qtd: $quantity")
                Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()
                binding.editProductName.text.clear()
                binding.editProductPrice.text.clear()
                binding.editProductDescription.text.clear()
                binding.editProductQuantity.text.clear()
                binding.editProductName.requestFocus()
            }
        }

        // Exemplo de recuperação de produtos
        binding.btnListarProdutos.setOnClickListener {
            // Recupera os produtos de forma assíncrona
            lifecycleScope.launch {
                val products = productDao.getAll()
                if (products.isNotEmpty()) {
                    val totalCount = products.size
                    val totalPrice = products.sumOf { it.price }
                    val sb = StringBuilder()
                    sb.append("Quantidade cadastrados: $totalCount | Soma preços: R$$totalPrice\n\n")
                    products.forEach {
                        Log.i("TAG", "ID = ${it.id}, Produto = ${it.name}, Preço = R$${it.price}, Qtd = ${it.quantity}, Descrição = ${it.description}")
                        sb.append("ID: ${it.id}\nNome: ${it.name}\nPreço: R$${it.price}\nQuantidade: ${it.quantity}\nDescrição: ${it.description ?: "N/A"}\n-------------------\n")
                    }
                    binding.textProductList.text = sb.toString()
                } else {
                    Log.i("TAG", "Nenhum produto encontrado.")
                    binding.textProductList.text = "Nenhum produto encontrado."
                }
            }
        }

        // Exemplo de busca por nome
        binding.btnBuscarProduto.setOnClickListener {
            val query = binding.editSearchName.text.toString()
            lifecycleScope.launch {
                val products = productDao.buscarPorNome("%$query%")
                if (products.isNotEmpty()) {
                    val totalCount = products.size
                    val totalPrice = products.sumOf { it.price }
                    val sb = StringBuilder()
                    sb.append("Resultados (Qtd: $totalCount | Total: R$$totalPrice)\n\n")
                    products.forEach {
                        sb.append("ID: ${it.id}\nNome: ${it.name}\nPreço: R$${it.price}\nQuantidade: ${it.quantity}\nDescrição: ${it.description ?: "N/A"}\n-------------------\n")
                    }
                    binding.textProductList.text = sb.toString()
                } else {
                    binding.textProductList.text = "Nenhum produto encontrado com esse nome."
                }
            }
        }

        // Excluir todos os produtos
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                binding.textProductList.text = "Todos os produtos foram excluídos."
                Toast.makeText(this@MainActivity, "Todos os produtos excluídos!", Toast.LENGTH_SHORT).show()
            }
        }

        // Atualizar preço do produto por ID
        binding.btnAtualizarProduto.setOnClickListener {
            val idText = binding.editUpdateId.text.toString()
            val priceText = binding.editUpdatePrice.text.toString()
            if (idText.isNotEmpty() && priceText.isNotEmpty()) {
                val id = idText.toIntOrNull() ?: 0
                val newPrice = priceText.toDoubleOrNull() ?: 0.0
                lifecycleScope.launch {
                    val p = productDao.getById(id)
                    if (p != null) {
                        val updated = p.copy(price = newPrice)
                        productDao.updateProduct(updated)
                        Toast.makeText(this@MainActivity, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                        binding.editUpdateId.text.clear()
                        binding.editUpdatePrice.text.clear()
                    } else {
                        Toast.makeText(this@MainActivity, "Produto não encontrado!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Preencha ID e novo preço!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
