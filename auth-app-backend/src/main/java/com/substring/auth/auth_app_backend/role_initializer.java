package com.substring.auth.auth_app_backend;

import com.substring.auth.auth_app_backend.Repository.RoleRepository;
import com.substring.auth.auth_app_backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class role_initializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Roles are added in table if not present");
        if(roleRepository.count()!=2){
            Role user = new Role();
            user.setName("USER");
            roleRepository.save(user);

            Role admin = new Role();
            admin.setName("ADMIN");
            roleRepository.save(admin);
            return;
        }
        System.out.println("Already exists in the table");
    }
}
