package com.infra.authorization.persistence.repository;

import com.infra.authorization.persistence.entities.ERole;
import com.infra.authorization.persistence.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(ERole name);
}