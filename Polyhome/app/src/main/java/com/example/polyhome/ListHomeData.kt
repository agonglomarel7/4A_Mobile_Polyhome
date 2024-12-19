package com.example.polyhome

data class ListHomeData(
    val houseId: Int,
    val owner: Boolean
) {
    override fun toString(): String {
        return "Maison ID: $houseId, Propriétaire: ${if (owner) "Oui" else "Non"}"
    }
}

