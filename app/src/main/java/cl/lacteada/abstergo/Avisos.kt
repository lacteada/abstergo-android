package cl.lacteada.abstergo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import retrofit2.HttpException

/** Un aviso simple, con el tipo de SweetAlert que se le pida. */
fun Context.aviso(titulo: String, texto: String, tipo: Int = SweetAlertDialog.ERROR_TYPE) {
    SweetAlertDialog(this, tipo)
        .setTitleText(titulo)
        .setContentText(texto)
        .setConfirmText("Aceptar")
        .setConfirmClickListener { it.dismissWithAnimation() }
        .show()
}

/** El título que corresponde a un error de la API. */
fun tituloDeError(e: Exception): String =
    if (e is HttpException && e.code() == 403) "Sin permiso" else "No se pudo completar"

/** El texto que corresponde a un error de la API. */
fun textoDeError(e: Exception, alternativo: String): String =
    if (e is HttpException && e.code() == 403) "Tu rol no permite esta acción." else alternativo

/** Un diálogo de progreso, para esperar una llamada de red. */
fun AppCompatActivity.dialogoProgreso(mensaje: String = "Un momento..."): SweetAlertDialog =
    SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
        titleText = mensaje
        setCancelable(false)
        show()
    }

/** Cierra un diálogo de progreso mostrando el resultado. */
fun SweetAlertDialog.terminar(
    tipo: Int,
    titulo: String,
    texto: String,
    boton: String = "Aceptar",
    alCerrar: () -> Unit = {},
) {
    changeAlertType(tipo)
    titleText = titulo
    contentText = texto
    confirmText = boton
    setConfirmClickListener { dialogo ->
        dialogo.dismissWithAnimation()
        alCerrar()
    }
}
