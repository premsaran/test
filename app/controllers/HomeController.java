package controllers;

import play.db.jpa.Transactional;
import entity.Account;
import entity.AccountBalance;
import entity.Currency;
import entity.Customer;
import entity.Menu;
import entity.RolePermissions;
import entity.Roles;
import entity.Status;
import entity.Tenant;
import entity.Transcation;
import entity.User;
import entity.UserTenant;
import play.Play;
import play.cache.CacheApi;
import play.data.Form;
import play.db.jpa.JPA;
import play.mvc.*;
import play.mvc.Http.MultipartFormData.FilePart;
import play.twirl.api.Format;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import beans.CurrencyBo;
import beans.Role;
import beans.UserBo;
import views.html.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class HomeController extends Controller {
	@Inject
	private CacheApi cache;

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewCustomer() {
		Query customerQuery=JPA.em().createQuery("select X from Customer X where X.tenant.tenantId=:tenantId");
		customerQuery.setParameter("tenantId",session("tenantId"));
		List<Customer> customerList=customerQuery.getResultList();
		return ok(createCustomer.render(customerList));
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewCurrency() {
		Query currencyQuery=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currencyQuery.getResultList();
		return ok(viewCurrency.render(currencyList));
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewUser() {

		Query userView = JPA.em().createQuery("select  X from User X order by update_date desc");
		List<User> userList=userView.getResultList();
		Query statusView = JPA.em().createQuery("select  X from Status X");
		List<Status> statusList=statusView.getResultList();
		Query tenantMapping = JPA.em().createQuery("select X from Tenant X");
		List<Tenant> tenant=tenantMapping.getResultList();
		return ok(createUser.render(userList,statusList,tenant));
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result createUser() {
		Date date = new Date();
		Form<?> userform=Form.form(UserBo.class);
		UserBo userBo = (UserBo) userform.bindFromRequest().get();
		for(User users:userBo.getUsers()){
			users.setUserId(UUID.randomUUID().toString());
			users.setPassword("test123");
			Status status=new Status();
			status.setStatusId(Play.application().configuration().getString("application.status.active.id"));
			users.setStatus(status);
			users.setForcePasswordChange("1");
			users.setUpdateDate(date);
			JPA.em().persist(users);
		}
		return redirect(routes.HomeController.viewUser());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result resetUser() {
		Form<?> userform=Form.form(User.class);
		User users=(User) userform.bindFromRequest().get();
		Query query=JPA.em().createNativeQuery("update users set password='test123',force_password_change='1' where user_id='"+users.getUserId()+"' and tenant_id=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		query.executeUpdate();
		return ok(login.render(""));
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result changePassword() {
		Form<?> changePasswordform=Form.form(User.class);
		User changeusers=(User) changePasswordform.bindFromRequest().get();
		Query query=JPA.em().createNativeQuery("update users set password='"+changeusers.getPassword()+"',force_password_change='2' where user_id='"+changeusers.getUserId()+"' and tenant_id=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		query.executeUpdate();
		return redirect(routes.HomeController.viewHome());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewResetUser() {
		Query userView=null;
		if(session("tenantId").equals(Play.application().configuration().getString("application.session.global.tenantId"))) 
			userView = JPA.em().createQuery("select  X from User X order by update_date desc");
		else {
			userView = JPA.em().createQuery("select  X from User X where X.tenant.tenantId=:tenantId order by update_date desc");
			userView.setParameter("tenantId",session("tenantId"));
		}
		List<User> userList=userView.getResultList();
		Query statusView = JPA.em().createQuery("select  X from Status X");
		List<Status> statusList=statusView.getResultList();
		Query tenantMapping = JPA.em().createQuery("select X from Tenant X");
		List<Tenant> tenant=tenantMapping.getResultList();
		return ok(resetUsers.render(userList,statusList,tenant));
	}
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
	public  Result resetUserPassword() {
		Query userView = JPA.em().createQuery("select  X from User X where X.tenant.tenantId=:tenantId order by update_date desc");
		userView.setParameter("tenantId",session("tenantId"));
		List<User> userList=userView.getResultList();
		Query statusView = JPA.em().createQuery("select  X from Status X");
		List<Status> statusList=statusView.getResultList();
		Query tenantMapping = JPA.em().createQuery("select X from Tenant X");
		List<Tenant> tenant=tenantMapping.getResultList();

		Form<?> changePasswordform=Form.form(User.class);
		User changeusers=(User) changePasswordform.bindFromRequest().get();
		String userId=changeusers.getUserId();
		String tenantId=changeusers.getTenant().getTenantId();
		String forcePasswordFlag=changeusers.getForcePasswordChange();
		Query query=JPA.em().createNativeQuery("update users set force_password_change='2' where tenant_id=:tenantId and user_id=:userId");
		query.setParameter("tenantId",tenantId);
		query.setParameter("userId",userId);
		query.executeUpdate();
		return redirect(routes.HomeController.viewResetUser());
	}
	@SuppressWarnings("deprecation")
	@Transactional
	public Result changeStatus() {
		Form<?> statusform=Form.form(User.class);
		User changeStatus=(User) statusform.bindFromRequest().get();
		Query query=JPA.em().createNativeQuery("update users set status_id='"+changeStatus.getStatus().getStatusId()+"' where user_id='"+changeStatus.getUserId()+"' and tenant_id=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		query.executeUpdate();
		return redirect(routes.HomeController.viewUser());
	}
	@SuppressWarnings("deprecation")
	@Transactional
	public Result changeMapingTenant() {
		Form<?> statusform=Form.form(UserTenant.class);
		UserTenant changeStatus=(UserTenant) statusform.bindFromRequest().get();
		String[] tenants=changeStatus.getTenant().getTenantId().split(",");
		String usersId=changeStatus.getUserId().getUserId();
		Query query=JPA.em().createNativeQuery("DELETE FROM user_tenant WHERE user_id ='"+usersId+"'");
		query.executeUpdate();
		for(int i=0;i<tenants.length;i++) {
			changeStatus = new UserTenant();
			changeStatus.setUserTenantId(UUID.randomUUID().toString());
			changeStatus.setUserId(JPA.em().find(User.class,usersId)); 
			Tenant tenant=new Tenant();
			tenant.setTenantId(tenants[i]);
			changeStatus.setTenant(tenant);
			JPA.em().persist(changeStatus); 
		}
		return redirect(routes.HomeController.viewUser());
	}
	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewRole() {
		Query  menuQuery=JPA.em().createQuery("select X from Menu X");
		List<Menu> menus=menuQuery.getResultList();
		Query  roleQuery=JPA.em().createQuery("select X from Roles X order by date desc");
		List<Roles> roles=roleQuery.getResultList();
		return ok(createRole.render(menus,roles));
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result createRole() {
		Date date=new Date();
		Form<?> createRoleForm=Form.form(Role.class);
		Role role=(Role) createRoleForm.bindFromRequest().get();
		String roleID=UUID.randomUUID().toString();
		Roles roles=role.getRoles();
		roles.setRoleId(roleID);
		roles.setDate(date);
		/*roles.setTenant(JPA.em().find(Tenant.class,session("tenantId")));*/
		roles.setTenant(JPA.em().find(Tenant.class,session("globalTenant")));
		JPA.em().persist(roles);
		for(RolePermissions rolePermissions:role.getRolePermissions()){
			/*rolePermissions.setTenant(JPA.em().find(Tenant.class,session("tenantId")));*/
			rolePermissions.setTenant(JPA.em().find(Tenant.class,session("globalTenant")));
			rolePermissions.setRole(roles);
			JPA.em().persist(rolePermissions);

		}
		return redirect(routes.HomeController.viewRole());
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result viewHome() {
		List<String> currencyValues = Arrays.asList(session("tenantId"),session("globalTenant"));
		Query currency=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currency.getResultList();
		Query accountQuery =JPA.em().createQuery("select X from Account X where X.tenant.tenantId=:tenantId");
		accountQuery.setParameter("tenantId", session("tenantId"));
		List<Account> accountList=accountQuery.getResultList();
		Query transactionQuery=JPA.em().createQuery("select X from AccountBalance X where X.account.accountCode='"+Play.application().configuration().getString("application.cash.accountId")+"' and X.currency.currencyId='"+session("currencyId")+"' and  X.tenant.tenantId=:tenantId");
		transactionQuery.setParameter("tenantId", session("tenantId"));
		List<AccountBalance> balList=transactionQuery.getResultList();
		String bal="";
		for(AccountBalance accountBalance:balList) {
			bal = ""+TransferController.removeExponentFromAmount((accountBalance.getCrAmount()-accountBalance.getDrAmount())+accountBalance.getOpeningBalance(),Long.parseLong(session("exponent")));
		}
		Transcation transcation = null;
		String check="";
		return ok(home.render(currencyList,transcation,bal,check,accountList));
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result createTransaction() {	
		Query currency=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currency.getResultList();
		return ok(createTransaction.render(currencyList));
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result viewTransfer() {
		Query transactionQuery=null;
		String statusActiveId=Play.application().configuration().getString("application.status.active.id");
		Query accountQuery=JPA.em().createQuery("select X from Account X where X.tenant.tenantId=:tenantId");
		accountQuery.setParameter("tenantId",session("tenantId"));
		List<Account> accountList=accountQuery.getResultList();
		Query currency=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currency.getResultList();
		transactionQuery=JPA.em().createQuery("select X from AccountBalance X where X.account.accountCode='"+Play.application().configuration().getString("application.cash.accountId")+"' and X.currency.currencyId='"+session("currencyId")+"' and  X.tenant.tenantId=:tenantId");
		transactionQuery.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> balList=transactionQuery.getResultList();
		String bal="";
		for(AccountBalance accountBalance:balList) {
			bal = ""+TransferController.removeExponentFromAmount(accountBalance.getOpeningBalance()+(accountBalance.getCrAmount()-accountBalance.getDrAmount()),Long.parseLong(session("exponent")));
		}
		return ok(viewTransfer.render(accountList,currencyList,bal));
	}

	@Transactional
	public Result viewReport() {
		return ok(report.render());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewLedgerReport() {

		Query statusQuery=JPA.em().createQuery("select X from Status X");
		List<Status> statusList = statusQuery.getResultList();
		return ok(ledgerReport.render(statusList));
	}

	@Transactional
	public Result home() {
		return ok(login.render(""));
	}
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result login() {
		session().clear();
		try {
			Form<?> loginform=Form.form(User.class);
			User user=(User) loginform.bindFromRequest().get();
			String loginid=user.getLoginId();
			String password=user.getPassword();
			Query query = JPA.em().createQuery("select X from User X where X.loginId=:loginId and X.password=:password");
			query.setParameter("loginId", loginid);
			query.setParameter("password", password);
			User results = (User) query.getSingleResult();
			session("loginId",loginid);
			session("exponent",Play.application().configuration().getString("application.session.exponent"));
			session("currencyExponent",Play.application().configuration().getString("application.currency.exponent"));
			session("sequenceName",Play.application().configuration().getString("application.report.transactionSeq.name"));
			session("globalTenant",Play.application().configuration().getString("application.session.global.tenantId"));
			session("dateFormat","dd-MM-yyyy HH:mm:ss");
			session("tenantId",results.getTenant().getTenantId());
			session("tenantName",results.getTenant().getTenantName());
			session("currencyId",results.getTenant().getCurrencyId().getCurrencyId());
			session("currencyName",results.getTenant().getCurrencyId().getCurrencyCode());
			session("userName",results.getUserName());
			session("userId",results.getUserId());
			Query query2=JPA.em().createQuery("select X from RolePermissions X where X.role.roleId=\'"+results.getRole().getRoleId()+"\' order by X.menu.menuId asc");
			List<RolePermissions> rolePermissions=query2.getResultList();
			cache.set(session("userName")+"_menus", rolePermissions);
			if(results.getStatus().getStatusId().equals(Play.application().configuration().getString("application.status.active.id"))) {
				if(results.getForcePasswordChange().equals("1")) 
					return ok(changePassword.render());
			}
			else
				return ok(login.render("false"));
			return redirect(routes.HomeController.viewHome());
		}
		catch(Exception e){
			return ok(login.render("false"));
		}
	}

	@Transactional
	public Result getBackImage() {
		try {
			Tenant results=JPA.em().find(Tenant.class, session("tenantId"));
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(results.getBackgroundImage()));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos);
			return ok(baos.toByteArray()).as("image/jpg");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ok();
	}

	@Transactional
	public Result logout() {
		session().clear();
		session().remove("userName");
		return ok(login.render(""));
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result switchTenant() {
		session().clear();
		String userId="",tenantId="";
		Form<?> loginform=Form.form(User.class);
		User user=(User) loginform.bindFromRequest().get();
		tenantId=user.getTenant().getTenantId();
		Tenant tenant=JPA.em().find(Tenant.class, tenantId);
		userId=user.getUserId();
		User user2=JPA.em().find(User.class, userId);
		session("tenantId",tenantId);
		session("userId",userId);   
		session("exponent",Play.application().configuration().getString("application.session.exponent"));
		session("globalTenant",Play.application().configuration().getString("application.session.global.tenantId"));
		session("currencyExponent",Play.application().configuration().getString("application.currency.exponent"));
		session("dateFormat","dd-MM-yyyy HH:mm:ss");
		session("userName",user2.getUserName());
		session("loginId",user2.getLoginId());
		session("tenantName",tenant.getTenantName());
		session("currencyId",tenant.getCurrencyId().getCurrencyId());
		session("currencyName",tenant.getCurrencyId().getCurrencyCode());
		session("userId",user2.getUserId());
		Query query2=JPA.em().createQuery("select X from RolePermissions X where X.role.roleId=\'"+user2.getRole().getRoleId()+"\' order by X.menu.menuId asc");
		List<RolePermissions> rolePermissions=query2.getResultList();
		cache.set(session("userName")+"_menus", rolePermissions);
		if(!user2.getStatus().getStatusId().equals(Play.application().configuration().getString("application.status.active.id"))) {
			return ok(login.render("false"));
		}
		return redirect(routes.HomeController.viewHome());
	}
	@SuppressWarnings("unchecked")
	@Transactional
	public Result tenantList() {
		JSONArray applicableTenants = new JSONArray();
		Query typedQuery = JPA.em().createNativeQuery("select  utenant.tenant_id,ten.tenant_name,utenant.user_id from user_tenant utenant join tenant ten on utenant.tenant_id=ten.tenant_id where utenant.user_id=:userId");
		typedQuery.setParameter("userId",session("userId"));
		List<Object[]> mappings= typedQuery.getResultList();
		for(Object[] objects:mappings) {
			JSONObject jsonObject= new JSONObject();
			jsonObject.put("userTenantId",objects[0].toString());
			jsonObject.put("userTenantName",objects[1].toString());
			jsonObject.put("userId",objects[2].toString());
			applicableTenants.put(jsonObject);
		}
		return ok(applicableTenants.toString());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result mappingTenants(String value)
	{
		JSONArray jsonArray = new JSONArray();
		Query query=JPA.em().createNativeQuery("select ten.tenant_name,utenant.tenant_id from tenant ten  join user_tenant utenant on utenant.tenant_id=ten.tenant_id  join users use on use.user_id=utenant.user_id where use.user_id=:userId and lower(ten.tenant_name) like :value");
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		query.setParameter("userId",session("userId"));
		List<Object[]> tenant=query.getResultList();
		for (Object[] tenant2 : tenant) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("data",tenant2[0].toString());
			jsonObject.put("value",tenant2[1].toString());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewSettings() {

		return ok(settings.render());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result uploadImage() throws Exception{
		Http.MultipartFormData body = request().body().asMultipartFormData();
		List<FilePart> resourceFiles = body.getFiles();
		if (resourceFiles != null) 
		{
			for (int i = 0; i < resourceFiles.size(); i++)
			{
				FilePart picture = body.getFile(resourceFiles.get(i).getKey());
				File file = (File) picture.getFile();
				Query query=JPA.em().createNativeQuery("update tenant SET background_image=:backgroundImage where tenant_id=:tenantId");
				query.setParameter("backgroundImage",com.google.common.io.Files.toByteArray(file));
				query.setParameter("tenantId",session("tenantId"));
				query.executeUpdate();
			}
		}else {
			Query query=JPA.em().createNativeQuery("select background_image from tenant where tenant_id=:tenantId");
			query.setParameter("tenantId",session("tenantId"));
			List<Object[]> mappings= query.getResultList();
			Query query1=JPA.em().createNativeQuery("delete from tenant where background_image=:backgroundImage and tenant_id=:tenantId");
			query1.setParameter("backgroundImage",mappings);
			query1.setParameter("tenantId",session("tenantId"));
			query1.executeUpdate();
		}
		return redirect(routes.HomeController.viewHome());
	}

}


