package cl.lacteada.abstergo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * La base local de usuarios.
 *
 * Guarda cada usuario con su estado de sincronización, así el listado funciona
 * sin conexión y los cambios pendientes sobreviven a cerrar la aplicación.
 */
class BdLocal(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOMBRE, null, VERSION) {

    override fun onCreate(bd: SQLiteDatabase) {
        bd.execSQL(CREAR_TABLA)
    }

    override fun onUpgrade(bd: SQLiteDatabase, anterior: Int, nueva: Int) {
        bd.execSQL("DROP TABLE IF EXISTS $TABLA")
        onCreate(bd)
    }

    fun usuarios(): List<UsuarioLocal> = readableDatabase
        .rawQuery("SELECT * FROM $TABLA ORDER BY nombre, apellido", null)
        .use { cursor -> cursor.generar { leer(cursor) } }

    fun pendientes(): List<UsuarioLocal> = readableDatabase
        .rawQuery("SELECT * FROM $TABLA WHERE estado != ?", arrayOf(UsuarioLocal.SINCRONIZADO))
        .use { cursor -> cursor.generar { leer(cursor) } }

    fun cantidadPendientes(): Int = readableDatabase
        .rawQuery("SELECT COUNT(*) FROM $TABLA WHERE estado != ?", arrayOf(UsuarioLocal.SINCRONIZADO))
        .use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    fun buscar(idLocal: String): UsuarioLocal? = readableDatabase
        .rawQuery("SELECT * FROM $TABLA WHERE id_local = ?", arrayOf(idLocal))
        .use { cursor -> if (cursor.moveToFirst()) leer(cursor) else null }

    fun guardar(usuario: UsuarioLocal) {
        writableDatabase.insertWithOnConflict(TABLA, null, valores(usuario), SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun eliminar(idLocal: String) {
        writableDatabase.delete(TABLA, "id_local = ?", arrayOf(idLocal))
    }

    fun marcarSincronizado(idLocal: String, idRemoto: Int) {
        val valores = ContentValues().apply {
            put("id_remoto", idRemoto)
            put("password", null as String?)
            put("estado", UsuarioLocal.SINCRONIZADO)
        }
        writableDatabase.update(TABLA, valores, "id_local = ?", arrayOf(idLocal))
    }

    /** Deja lo del servidor sin tocar lo que está pendiente. */
    fun reemplazarSincronizados(remotos: List<UsuarioLocal>) {
        val bd = writableDatabase
        bd.beginTransaction()
        try {
            bd.delete(TABLA, "estado = ?", arrayOf(UsuarioLocal.SINCRONIZADO))
            remotos.forEach { bd.insertWithOnConflict(TABLA, null, valores(it), SQLiteDatabase.CONFLICT_REPLACE) }
            bd.setTransactionSuccessful()
        } finally {
            bd.endTransaction()
        }
    }

    private fun valores(usuario: UsuarioLocal) = ContentValues().apply {
        put("id_local", usuario.idLocal)
        put("id_remoto", usuario.idRemoto)
        put("nombre", usuario.nombre)
        put("apellido", usuario.apellido)
        put("email", usuario.email)
        put("password", usuario.password)
        put("activo", if (usuario.activo) 1 else 0)
        put("estado", usuario.estado)
        put("actualizado", usuario.actualizado)
    }

    private fun leer(cursor: android.database.Cursor) = UsuarioLocal(
        idLocal = cursor.getString(cursor.getColumnIndexOrThrow("id_local")),
        idRemoto = cursor.getInt(cursor.getColumnIndexOrThrow("id_remoto"))
            .takeIf { !cursor.isNull(cursor.getColumnIndexOrThrow("id_remoto")) },
        nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
        apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
        email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
        password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
        activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
        actualizado = cursor.getLong(cursor.getColumnIndexOrThrow("actualizado")),
    )

    private inline fun <T> android.database.Cursor.generar(bloque: () -> T): List<T> {
        val elementos = mutableListOf<T>()
        while (moveToNext()) elementos.add(bloque())
        return elementos
    }

    private companion object {
        const val NOMBRE = "abstergo_local.db"
        const val VERSION = 1
        const val TABLA = "usuarios"
        const val CREAR_TABLA = """
            CREATE TABLE $TABLA (
                id_local TEXT PRIMARY KEY,
                id_remoto INTEGER,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                email TEXT NOT NULL,
                password TEXT,
                activo INTEGER NOT NULL DEFAULT 1,
                estado TEXT NOT NULL,
                actualizado INTEGER NOT NULL
            )
        """
    }
}
