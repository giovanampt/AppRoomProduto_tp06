package br.unisanta.approomproduto.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.unisanta.approomproduto.dao.ProductDao
import br.unisanta.approomproduto.model.Product

@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
