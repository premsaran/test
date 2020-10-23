package entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="menu")
@Access(AccessType.FIELD)
public class Menu implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="menu_id",nullable=false)
	String menuId;
	
	@Column(name="menu_name",nullable=false)
	String menuName;
	
	@Column(name="fontawesome_name",nullable=false)
	String fontAwesomeName;

	@Column(name="level",nullable=false)
	String level;
	
	@Column(name="url",nullable=false)
	String url;
	
	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getFontAwesomeName() {
		return fontAwesomeName;
	}

	public void setFontAwesomeName(String fontAwesomeName) {
		this.fontAwesomeName = fontAwesomeName;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
