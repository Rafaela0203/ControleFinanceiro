package br.edu.utfpr.controlefinanceiro.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.edu.utfpr.controlefinanceiro.R

object NotificationHelper {

    // 1. Constantes para o Canal de Notificação
    // Você pode alterar isso, mas lembre dos nomes que usar.
    private const val CHANNEL_ID = "goal_alerts_channel"
    private const val CHANNEL_NAME = "Alertas de Meta"
    private const val CHANNEL_DESCRIPTION = "Notificações para metas financeiras atingidas"

    /**
     * Cria o Canal de Notificação.
     * Isso deve ser chamado uma vez, quando o app iniciar (ex: na MainActivity).
     */
    fun createNotificationChannel(context: Context) {
        // A criação do canal só é necessária no Android 8.0 (API 26) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            // Registra o canal no sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Constrói e exibe a notificação de alerta de meta.
     * @param context Contexto (Fragment ou Activity)
     * @param title Título da notificação (ex: "Meta Atingida!")
     * @param message Mensagem (ex: "Você atingiu seu limite para a meta 'Transporte'")
     */
    fun sendGoalAlertNotification(context: Context, title: String, message: String) {
        // Um ID único para esta notificação. Se você usar o mesmo ID,
        // a notificação anterior será atualizada.
        val notificationId = System.currentTimeMillis().toInt()

        // Usamos o 'ic_add' como ícone, pois já existe no seu projeto.
        // O ideal seria ter um ícone específico para alertas (ex: um sino).
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_add) // Mude para um ícone de alerta se tiver
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Remove a notificação quando o usuário toca nela

        // Exibe a notificação
        // 'NotificationManagerCompat' cuida da compatibilidade com versões antigas
        with(NotificationManagerCompat.from(context)) {
            // A permissão (POST_NOTIFICATIONS) será tratada na MainActivity.
            // Se ela não for concedida, a notificação não será exibida (silenciosamente).
            notify(notificationId, builder.build())
        }
    }
}