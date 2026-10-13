package cl.lacteada.abstergo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.lacteada.abstergo.data.Atencion

class AtencionAdapter(
    private val describir: (Atencion) -> String,
    private val alTocar: (Atencion) -> Unit,
) : RecyclerView.Adapter<AtencionAdapter.Fila>() {

    private var datos: List<Atencion> = emptyList()

    fun setDatos(nuevos: List<Atencion>) {
        datos = nuevos
        notifyDataSetChanged()
    }

    class Fila(vista: View) : RecyclerView.ViewHolder(vista) {
        val texto: TextView = vista.findViewById(R.id.filaAtencion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atencion, parent, false)
        return Fila(vista)
    }

    override fun getItemCount(): Int = datos.size

    override fun onBindViewHolder(holder: Fila, position: Int) {
        val atencion = datos[position]
        holder.texto.text = describir(atencion)
        holder.texto.setOnClickListener { alTocar(atencion) }
    }
}
