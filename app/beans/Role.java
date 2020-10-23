package beans;


import java.util.List;

import entity.RolePermissions;
import entity.Roles;

public class Role {

	Roles roles;
	
	List<RolePermissions> rolePermissions;

	public List<RolePermissions> getRolePermissions() {
		return rolePermissions;
	}

	public void setRolePermissions(List<RolePermissions> rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	public Roles getRoles() {
		return roles;
	}

	public void setRoles(Roles roles) {
		this.roles = roles;
	}
	
}
