package com.gp.core;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gp.common.AccessPoint;
import com.gp.common.FlatColumns;
import com.gp.common.Operations;
import com.gp.common.GPrincipal;
import com.gp.common.IdKeys;
import com.gp.common.ServiceContext;
import com.gp.exception.CoreException;
import com.gp.exception.ServiceException;
import com.gp.dao.info.DictionaryInfo;
import com.gp.svc.DictionaryService;
import com.gp.validate.ValidateMessage;
import com.gp.validate.ValidateUtils;

@Component
public class DictionaryFacade {

	static Logger LOGGER = LoggerFactory.getLogger(DictionaryFacade.class);
	
	public static final String LANG_EN_US = "en_US";
	public static final String LANG_FR_FR = "fr_FR";
	public static final String LANG_ZH_CN = "zh_CN";
	public static final String LANG_DE_DE = "de_DE";
	public static final String LANG_RU_RU = "ru_RU";
	
	private static DictionaryService dictservice;
	
	@Autowired
	private DictionaryFacade(DictionaryService dictservice){
		DictionaryFacade.dictservice = dictservice;
	}

	/**
	 * Find the dictionary entries from database.
	 * @param groupkey the entry group key
	 * @param language the language expected 
	 **/
	public static List<DictionaryInfo> findDictEntries(AccessPoint accesspoint,
			GPrincipal principal, String groupkey, String keyFilter) throws CoreException{
		
		List<DictionaryInfo> gresult = null;
		
		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_DICTS)){

			gresult = dictservice.getDictEntries(svcctx, groupkey, keyFilter);
			
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e);
			
		}finally{
			
			ContextHelper.handleContext();
		}
		return gresult;
	}
	
	public static Boolean saveDictEntry(AccessPoint accesspoint,
			GPrincipal principal, DictionaryInfo dictinfo) throws CoreException{
		
		if(!IdKeys.isValidId(dictinfo.getInfoId())){
			CoreException cexcp = new CoreException(principal.getLocale(), "excp.save.dict");
			cexcp.addValidateMessage("prop.dictid", "mesg.prop.miss");
			throw cexcp;
		}
		
		boolean state = false;
		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.UPDATE_DICT)){
			
			svcctx.setOperationObject(dictinfo.getInfoId());
			svcctx.addOperationPredicates(dictinfo);
			
			Set<ValidateMessage> vmsg = ValidateUtils.validate(principal.getLocale(), dictinfo);
			if(null != vmsg && vmsg.size() > 0){ // fail pass validation
				CoreException cexcp = new CoreException(principal.getLocale(), "excp.save.dict");			
				cexcp.addValidateMessages(vmsg);
				throw cexcp;
			}
			
			state = dictservice.updateDictEntry(svcctx, dictinfo);
		} catch (Exception e) {
			
			LOGGER.error("Fail query dict entry",e);
			ContextHelper.stampContext(e);
	
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return state;
	}
	
	/**
	 * Get the message pattern from dictionary 
	 * @param locale the locale setting
	 * @param dictKey the key of dictionary entry, eg. excp.find.workgroup 
	 **/
	public static String findMessagePattern(Locale locale, String dictKey){
		
		DictionaryInfo dinfo = dictservice.getDictEntry(StringUtils.lowerCase(dictKey), false);
		if(dinfo == null) return dictKey;
		String msgptn = null;
		if(LANG_EN_US.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_EN_US);
		}else if(LANG_ZH_CN.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_ZH_CN);
		}else if(LANG_FR_FR.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_FR_FR);
		}else if(LANG_DE_DE.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_DE_DE);
		}else if(LANG_RU_RU.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_RU_RU);
		}else{
			msgptn = dictKey;
		}
		
		return msgptn;
	}
	
	/**
	 * Find the bean property name, user hibernate validator to check the data.
	 * but the property name is bean property, here convert the bean property name 
	 * to localized string. eg. sourceId = 来源ID
	 * 
	 * @param locale the locale setting
	 * @param dictKey the key of dictionary entry
	 **/
	public static String findPropertyName(Locale locale, String dictKey){
		
		String newkey = StringUtils.lowerCase(dictKey);
		if(!StringUtils.startsWith(newkey, "prop.")){
			newkey = "prop." + newkey;
		}
		DictionaryInfo dinfo = dictservice.getDictEntry(newkey, true);
		if(dinfo == null) return dictKey;
		String msgptn = null;
		if(LANG_EN_US.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_EN_US);
		}else if(LANG_ZH_CN.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_ZH_CN);
		}else if(LANG_FR_FR.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_FR_FR);
		}else if(LANG_DE_DE.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_DE_DE);
		}else if(LANG_RU_RU.equals(locale.toString())){
			msgptn = dinfo.getLabel(FlatColumns.DICT_RU_RU);
		}else{
			msgptn = dictKey;
		}
		
		return msgptn;
	}
}
