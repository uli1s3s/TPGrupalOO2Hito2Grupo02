package com.unla.ghsicilianotfi.services.implementation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.entities.Rol;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;

@Service("userService")
public class UserService implements UserDetailsService {

	private final IUsuarioRepository usuarioRepository;

	public UserService(IUsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String nombre)
			throws UsernameNotFoundException {

		Usuario usuario = usuarioRepository.findByNombreConRol(nombre)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Usuario no encontrado: " + nombre
				));

		return buildUser(
				usuario,
				buildGrantedAuthorities(usuario.getRol())
		);
	}
	private User buildUser(Usuario usuario,
						   List<GrantedAuthority> grantedAuthorities) {

		return new User(
				usuario.getNombre(),
				usuario.getContrasena(),
				usuario.getActivo(),
				true,
				true,
				true,
				grantedAuthorities
		);
	}

	private List<GrantedAuthority> buildGrantedAuthorities(Rol rol) {

		String nombreRol = "ROLE_" + rol.getNombre();

		return List.of(
				new SimpleGrantedAuthority(nombreRol)
		);
	}
}
