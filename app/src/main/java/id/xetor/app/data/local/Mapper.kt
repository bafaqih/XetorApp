package id.xetor.app.data.local

import id.xetor.app.data.remote.UserDto

fun UserDto.mapToEntity(): User {
    return User(
        id = this.id.toString(),
        name = this.fullname,
        email = this.email,
        phone = this.phone, // Map field baru
        photo = this.photo  // Map field baru
    )
}