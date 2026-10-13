package com.example.proyectopenka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopenka.data.Vecino

class VecinoAdapter(private val alTocar: (Vecino) -> Unit) :
    RecyclerView.Adapter<VecinoAdapter.Fila>() {

    private var datos: List<Vecino> = emptyList()

    fun setDatos(nuevos: List<Vecino>) {
        datos = nuevos
        notifyDataSetChanged()
    }

    class Fila(vista: View) : RecyclerView.ViewHolder(vista) {
        val texto: TextView = vista.findViewById(R.id.filaVecino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vecino, parent, false)
        return Fila(vista)
    }

    override fun getItemCount(): Int = datos.size

    override fun onBindViewHolder(holder: Fila, position: Int) {
        val vecino = datos[position]
        holder.texto.text = "${vecino.nombre} · ${vecino.rut} · ${vecino.estado}"
        holder.texto.setOnClickListener { alTocar(vecino) }
    }
}
