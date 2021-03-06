package br.edu.ifs.project_web.controller;

import java.sql.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.ifs.project_web.dto.UsuarioCreate;
import br.edu.ifs.project_web.dto.UsuarioDTO;
import br.edu.ifs.project_web.model.Usuario;
import br.edu.ifs.project_web.repository.UsuarioRepository;
import br.edu.ifs.project_web.service.LogService;
import br.edu.ifs.project_web.service.PerfilService;
import br.edu.ifs.project_web.service.UsuarioService;
import br.edu.ifs.project_web.util.ResponseDefault;


import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
	
    ResponseDefault response = new ResponseDefault();
	private String logExecucao;
    
	@Autowired
    UsuarioService usuarioService;
	@Autowired
	LogService logService;
	@Autowired
	PerfilService perfilService;
	@Autowired
	UsuarioRepository repository;

    
    
    @GetMapping(value = "/usuByLogin")
    public Object getByLogin(HttpServletRequest request, @RequestParam String usuTxLogin) {
    	UsuarioDTO usu = usuarioService.getUsuHeader(request);
    	if(usu.getPerNrId().getPerNrId()<2) {
    		logService.criar(usu.getUsuNrId(), "Tentou buscar um usuário");
    		return null;
    	}
    	try {
            if(repository.findByUsuTxLogin(usuTxLogin) == null) {
                response.setCodigo(400);
                response.setMensagem("Login não encontrado");
                response.setValue(false);
                logExecucao=("Tentativa de buscar usuario por login com erro.");
                logService.criar(usu.getUsuNrId(), logExecucao);
                return response;
            }else {
                logService.criar(usu.getUsuNrId(), "Buscou o usuário: "+usuTxLogin);
                return repository.findByUsuTxLogin(usuTxLogin);
            }
    	} catch (Exception e) {
    		response.setCodigo(400);
            response.setMensagem("Login não encontrado");
            response.setValue(false);
            logExecucao=("Tentativa de buscar usuario por login com erro.");
            logService.criar(usu.getUsuNrId(), logExecucao);
            return response;
    	}
    }

    @GetMapping(value = "/usuByToken")
    public Object getByToken(HttpServletRequest request) {
        UsuarioDTO usu = usuarioService.getUsuHeader(request);
        try {
            return usu;
        } catch (NoSuchElementException e) {
            response.setCodigo(400);
            response.setMensagem("Usuario não encontrado");
            response.setValue(false);
            return response;
        }
    }

    @GetMapping(value = "/usuByLoginExist")
    public Object getByLoginExist(@RequestParam String usuTxLogin) {
        Boolean existe = false;
        try {
            logService.criar(0, "Sistema Buscou o usuário: "+usuTxLogin);
            usuarioService.getByLogin(usuTxLogin);
            return existe = true;
        } catch (NoSuchElementException e) {
            response.setCodigo(400);
            response.setMensagem("Usuario não encontrado");
            response.setValue(false);
            logExecucao=("Sistema não encontrou usuário: "+usuTxLogin+".");
            logService.criar(0, logExecucao);
            return existe;
        }
    }
    
    @GetMapping(value = "/usuById")
    public Object getById(HttpServletRequest request, @RequestParam Integer usuNrId) {
    	UsuarioDTO usu = usuarioService.getUsuHeader(request);
    	if(usu.getPerNrId().getPerNrId()<2) {
    		logService.criar(usu.getUsuNrId(), "Tentou buscar um usuário");
    		return null;
    	}
    	try {
    	if(repository.findById(usuNrId) == null) {
    		response.setCodigo(400);
            response.setMensagem("Id não encontrado!");
            response.setValue(false);
            logExecucao=("Tentativa de buscar um usuario por ID com erro.");
            logService.criar(usu.getUsuNrId(), logExecucao);
            return response;
    	}else {
        	logService.criar(usu.getUsuNrId(), "Buscou o usuário: "+usuNrId);
            return repository.findById(usuNrId);
    	}
    	} catch (Exception e) {
    		response.setCodigo(400);
            response.setMensagem(e.getMessage());
            response.setValue(false);
            logExecucao=("Tentativa de buscar um usuario por ID com erro.");
            logService.criar(usu.getUsuNrId(), logExecucao);
            return response;
    	}
    }

    @GetMapping("/listarTodos")
    public ResponseEntity<List<Usuario>> listarTodos(HttpServletRequest request) {
    	UsuarioDTO usu = usuarioService.getUsuHeader(request);
    	if(usu.getPerNrId().getPerNrId()<2) {
    		logService.criar(usu.getUsuNrId(), "Tentou listar todos os usuários");
    		return null;
    	}
    	logService.criar(usu.getUsuNrId(), "Listou todos os usuários");
        return ResponseEntity.ok((List<Usuario>)repository.findAll());
    }

    @PutMapping("/salvar")
    public ResponseEntity<Usuario> salvar(@RequestBody Usuario usuario, HttpServletRequest request) {
    	UsuarioDTO usu = usuarioService.getUsuHeader(request);
    	if(usu.getPerNrId().getPerNrId()<2) {
    		logService.criar(usu.getUsuNrId(), "Tentou fazer update em um usuário.");
    		return null;
    	}
		logService.criar(usu.getUsuNrId(), "Fez update no usuário: "+usuario.getUsuNrId());
        return ResponseEntity.ok(repository.save(usuario));
    }


    @PutMapping("/salvarNome")
    public ResponseEntity<Usuario> salvarNome(@RequestBody String novoNome, HttpServletRequest request) {
        Usuario usu = usuarioService.getUsuarioHeader(request);
        if(usu.getPerNrId().getPerNrId()<2) {
            logService.criar(usu.getUsuNrId(), "Tentou atualizar seu nome.");
            return null;
        }
        logService.criar(usu.getUsuNrId(), "Atualizou seu nome: "+novoNome);
        usu.setUsuTxNome(novoNome);
        return ResponseEntity.ok(repository.save(usu));
    }

    @GetMapping("/validarSenha")
    public ResponseEntity<Boolean> validarSenha(@RequestParam String login, @RequestParam String password) {
        Optional<Usuario> optUsuario = repository.findByUsuTxLogin(login);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        Usuario usuario = optUsuario.get();
        boolean valid=false;
        if(password==usuario.getUsuTxSenha()) {
        	valid=true;
        }
        HttpStatus status = (valid) ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        logService.criar(usuario.getUsuNrId(), "Senha validada.");
        return ResponseEntity.status(status).body(valid);
    }
    
    @PostMapping(value = "/criarUsuario")
    public Object criarUsuario(@RequestBody UsuarioCreate usuarioCreate) throws Throwable {
        Usuario usuario = new Usuario();
        try {
            if(usuarioService.loginExiste(usuarioCreate.getUsuTxLogin()) != null){
                response.setCodigo(400);
                response.setMensagem("Login já existente");
                response.setValue(false);
                logExecucao=("Tentativa de cadastro com erro.");
                logService.criar(0, logExecucao);
                return response;
            }
            usuario = usuarioCreate.toUsuario();
        	usuario.setPerNrId(perfilService.findByPerNrId(1));
        	usuario.setUsuTxToken("0");
        	usuario.setUsuTxStatus('A');
        	usuario.setUsuDtCadastro(new Date(System.currentTimeMillis()));
            usuarioService.criar(usuario);
            response.setValue(true);
            response.setCodigo(200);
            response.setMensagem("Usuario: "+usuario.getUsuTxLogin()+" criado.");
            logExecucao="Novo usuario se cadastrou: " + usuario.getUsuTxLogin();
            logService.criar(usuario.getUsuNrId(), logExecucao);
        } catch (Exception e) {
            response.setCodigo(400);
            e.printStackTrace();
            response.setMensagem(e.getMessage());
            response.setValue(false);
            logExecucao=("Tentativa de cadastro com erro.");
            logService.criar(usuario.getUsuNrId(), logExecucao);
        }
        return response;
    }
    
    @DeleteMapping(value = "/deletarUsuario")
    public Object deletarUsuario(@RequestBody String login, HttpServletRequest request) throws Throwable {
    	UsuarioDTO usu = usuarioService.getUsuHeader(request);
    	if(usu.getPerNrId().getPerNrId()<2) {
    		logService.criar(usu.getUsuNrId(), "Tentou criar um usuário.");
    		response.setCodigo(400);
            response.setMensagem("Rank insuficiente");
            response.setValue(false);
    	}
        try {
        	Usuario usuario = usuarioService.getByLoginUsu(login);
            repository.delete(usuario);
            response.setValue(true);
            response.setCodigo(200);
            response.setMensagem("Usuario: "+usuario.getUsuTxLogin()+" deletado.");
            logExecucao="Usuario deletado: " + usuario.getUsuTxLogin();
            logService.criar(usu.getUsuNrId(), logExecucao);
        } catch (Exception e) {
            response.setCodigo(400);
            e.printStackTrace();
            response.setMensagem(e.getMessage());
            response.setValue(false);
            logExecucao=("Tentativa de deletar usuario com erro.");
            logService.criar(usu.getUsuNrId(), logExecucao);
        }
        return response;
    }
    
}