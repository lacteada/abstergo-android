package cl.lacteada.abstergo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Un listado de una línea: un texto y un toque. */
class ListaAdapter<T>(
    private val describir: (T) -> String,
    private val alTocar: (T) -> Unit,
) : RecyclerView.Adapter<ListaAdapter.Fila>() {

    private var datos: List<T> = emptyList()

    fun setDatos(nuevos: List<T>) {
        datos = nuevos
        notifyDataSetChanged()
    }

    class Fila(vista: View) : RecyclerView.ViewHolder(vista) {
        val texto: TextView = vista.findViewById(R.id.filaTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila =
        Fila(LayoutInflater.from(parent.context).inflate(R.layout.item_lista, parent, false))

    override fun getItemCount(): Int = datos.size

    override fun onBindViewHolder(holder: Fila, position: Int) {
        val elemento = datos[position]
        holder.texto.text = describir(elemento)
        holder.texto.setOnClickListener { alTocar(elemento) }
    }
}
