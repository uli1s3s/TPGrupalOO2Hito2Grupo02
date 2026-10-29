package com.unla.ghsicilianotfi.helpers;

public class ViewRouteHelper {
	/**** Views ****/
	//HOME
	public final static String INDEX = "home/index";
	public final static String HELLO = "home/hello";

	//DEGREE
	public final static String DEGREE_INDEX = "degree/index";
	public final static String DEGREE_FORM = "degree/form";
	public final static String DEGREE_NEW = "degree/new";

	//PERSON
	public final static String PERSON_INDEX = "person/index";
	public final static String PERSON_NEW = "person/new";
	public final static String PERSON_UPDATE = "person/update";
	public final static String PERSON_PARTIAL_VIEW = "person/partialPersonView";

	//USER
	public final static String USER_LOGIN = "user/login";
	public final static String USER_LOGOUT = "user/logout";

	//UNIDAD DE VENTA
	public final static String UNIDAD_NUEVA = "unidad/nueva";
	public final static String UNIDAD_LISTA = "unidad/lista";
	public final static String MI_UNIDAD = "unidad/mi-unidad";

	//FESTIVAL
	public final static String FESTIVAL_INDEX = "festival/index";
	public final static String FESTIVAL_FORM = "festival/form";
	
	public final static String REGISTRO_RESPONSABLE_EXITO = "unidad/registro-exito";
	
	//LIQUIDACION
	public final static String LIQUIDACION_LISTA = "liquidacion/lista";

	//RECIBO DE SUELDO
	public final static String RECIBO_LISTA = "recibos/mis-recibos";
	public final static String RECIBO_ROOT = "/recibos";
	
	//REPORTES
	public final static String REPORTE_MAYOR_CANON = "reportes/mayor-canon";
	
	//EMPLEADO
	public final static String EMPLEADO_INDEX = "empleados/index";
	public final static String EMPLEADO_FORM = "empleados/form";

	//PLATO
	public final static String PLATO_INDEX = "platos/index";
	public final static String PLATO_FORM = "platos/form";

	/**** Redirects ****/
	public final static String ROUTE = "/index";
	public final static String DEGREE_ROOT = "/degrees/";
	public final static String PERSON_ROOT = "/person";
	public final static String UNIDAD_ROOT = "/unidades";
	public final static String MI_UNIDAD_ROOT = "/mi-unidad";
	
	public final static String REGISTRO_RESPONSABLE_ROOT = "/registro-responsable";
	
	public final static String EMPLEADO_ROOT = "/empleados";
	public final static String PLATO_ROOT = "/platos";
}
