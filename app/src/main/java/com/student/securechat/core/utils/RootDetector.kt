package com.student.securechat.core.utils

import android.content.Context
import android.util.Log
import com.scottyab.rootbeer.RootBeer

object RootDetector {

    private const val TAG = "RootDetector"

    /**
     * FONCTION PRINCIPALE D'ACTION
     * Utilise la librairie RootBeer pour une détection fiable et détaillée.
     */
    fun isDeviceRooted(context: Context): Boolean {
        val rootBeer = RootBeer(context)

        // On lance une vérification détaillée pour voir exactement quelle méthode détecte le root.
        val isRooted = rootBeer.isRooted

        if (isRooted) {
            Log.e(TAG, "L'appareil est DÉTECTÉ comme étant rooté !")
            Log.d(TAG, "Vérification des binaires SU: " + if(rootBeer.checkForSuBinary()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification de l'existence de SU: " + if(rootBeer.checkSuExists()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification des chemins en lecture/écriture: " + if(rootBeer.checkForRWPaths()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification des propriétés dangereuses: " + if(rootBeer.checkForDangerousProps()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification native du root: " + if(rootBeer.checkForRootNative()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification du binaire Magisk: " + if(rootBeer.checkForMagiskBinary()) "TROUVÉ" else "non trouvé")
            Log.d(TAG, "Vérification du binaire BusyBox: " + if(rootBeer.checkForBusyBoxBinary()) "TROUVÉ" else "non trouvé")
        } else {
            Log.i(TAG, "L'appareil n'est pas détecté comme étant rooté.")
        }

        return isRooted
    }
}