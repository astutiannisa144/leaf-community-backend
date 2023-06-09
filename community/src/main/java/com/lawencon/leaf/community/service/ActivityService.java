package com.lawencon.leaf.community.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.dao.ActivityDao;
import com.lawencon.leaf.community.dao.ActivityTypeDao;
import com.lawencon.leaf.community.dao.CategoryDao;
import com.lawencon.leaf.community.dao.FileDao;
import com.lawencon.leaf.community.dao.ScheduleDao;
import com.lawencon.leaf.community.dao.UserActivityDao;
import com.lawencon.leaf.community.dao.UserDao;
import com.lawencon.leaf.community.model.Activity;
import com.lawencon.leaf.community.model.ActivityType;
import com.lawencon.leaf.community.model.Category;
import com.lawencon.leaf.community.model.File;
import com.lawencon.leaf.community.model.Schedule;
import com.lawencon.leaf.community.model.User;
import com.lawencon.leaf.community.model.UserActivity;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.activity.PojoActivityReq;
import com.lawencon.leaf.community.pojo.activity.PojoActivityReqGetAll;
import com.lawencon.leaf.community.pojo.activity.PojoActivityRes;
import com.lawencon.leaf.community.pojo.activity.type.PojoActivityTypeRes;
import com.lawencon.leaf.community.pojo.schedule.PojoScheduleRes;
import com.lawencon.leaf.community.util.DateUtil;
import com.lawencon.leaf.community.util.GenerateCodeUtil;
import com.lawencon.security.principal.PrincipalService;

@Service
public class ActivityService extends BaseService<PojoActivityRes> {
	private final ActivityDao activityDao;
	private final CategoryDao categoryDao;
	private final FileDao fileDao;
	private final UserDao userDao;
	private final ActivityTypeDao activityTypeDao;
	private final ScheduleDao scheduleDao;
	@Autowired
	private UserActivityDao userActivityDao;
	private final PrincipalService principalService;

	public ActivityService(ActivityDao activityDao, ActivityTypeDao activityTypeDao, CategoryDao categoryDao,
			FileDao fileDao, UserDao userDao, ScheduleDao scheduleDao, PrincipalService principalService) {
		this.activityDao = activityDao;
		this.activityTypeDao = activityTypeDao;
		this.categoryDao = categoryDao;
		this.fileDao = fileDao;
		this.userDao = userDao;
		this.scheduleDao = scheduleDao;
		this.principalService = principalService;
	}
	
	private void valIdNull(PojoActivityReq activity) {
		if(activity==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(activity.getId()!=null ) {
			throw new RuntimeException("Id must be empty");
		}
	}


	
	private void valBkNotNull(Activity activity) {
		if(activity==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(activity.getActivityCode()==null) {
			throw new RuntimeException("Invoice cannot be empty");
		}
	}


	private void valNonBk(PojoActivityReq activity) {
		if(activity==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(activity.getCategoryId()==null) {
			throw new RuntimeException("Member cannot be empty");
		}
		if(activity.getActivityTypeId()==null) {
			throw new RuntimeException("Activity Type cannot be empty");
		}
		if(activity.getPrice()==null) {
			throw new RuntimeException("Price cannot be empty");
		}

		if((activity.getProvider()==null)) {
			throw new RuntimeException("Provider cannot be empty");

		}
		
	}
	private void valIdNotNull(PojoActivityReq activity) {
		if(activity.getId()==null) {
			throw new RuntimeException("Id cannot be empty");
		}
	}
	
	private void valIdExist(String id) {
		if(activityDao.getById(id).isEmpty()) {
			throw new RuntimeException("Id cannot be empty in database");
		}
	}

	@Override
	public PojoActivityRes getById(String id) {
		Optional<Activity> activity = activityDao.getById(id);
		PojoActivityRes activitiesRes = new PojoActivityRes();
		List<Schedule> schedules= scheduleDao.getAll();

		if (activity.isEmpty()) {
			throw new RuntimeException("This is Empty or lacking");
		}
		activitiesRes.setActivityCode(activity.get().getActivityCode());
		activitiesRes.setActivityTypeId(activity.get().getActivityType().getId());
		activitiesRes.setActivityTypeCode(activity.get().getActivityType().getActivityTypeCode());
		activitiesRes.setActivityTypeName(activity.get().getActivityType().getActivityTypeName());
		activitiesRes.setCategoryId(activity.get().getCategory().getId());
		activitiesRes.setCategoryCode(activity.get().getCategory().getCategoryCode());
		activitiesRes.setCategoryName(activity.get().getCategory().getCategoryName());
		activitiesRes.setDescription(activity.get().getDescription());
		activitiesRes.setTitle(activity.get().getTitle());
		activitiesRes.setFileId(activity.get().getFile().getId());
		activitiesRes.setFileContent(activity.get().getFile().getFileContent());
		activitiesRes.setFileExtension(activity.get().getFile().getFileExtension());
		activitiesRes.setMemberId(activity.get().getMember().getId());
		activitiesRes.setFullName(activity.get().getMember().getProfile().getFullName());
		activitiesRes.setId(activity.get().getId());
		activitiesRes.setLocationAddress(activity.get().getLocationAddress());
		activitiesRes.setPrice(activity.get().getPrice());
		activitiesRes.setProvider(activity.get().getProvider());
		activitiesRes.setTimeEnd(activity.get().getTimeEnd());
		activitiesRes.setTimeStart(activity.get().getTimeStart());
		activitiesRes.setVer(activity.get().getVer());
		activitiesRes.setCreatedAt(DateUtil.dateToStr(activity.get().getCreatedAt()) );
		List<PojoScheduleRes> scheduleRes = new ArrayList<>();
		for(int j=0;j<schedules.size();j++) {
			if(schedules.get(j).getActivity().getId().equals(activity.get().getId()) ) {
				PojoScheduleRes schedule = new PojoScheduleRes();
				schedule.setId(schedules.get(j).getId());
				schedule.setScheduleDate(schedules.get(j).getScheduleDate());
				schedule.setVer(schedules.get(j).getVer());
				scheduleRes.add(schedule);
			}
		}
		activitiesRes.setSchedule(scheduleRes);
		return activitiesRes;
	}

	public List<PojoActivityRes> getAll(String typeCode,String categoryId,String code,int limit,int page) {
		List<Activity> activities = new ArrayList<>();
		List<UserActivity>userActivities=new ArrayList<>();
		if (categoryId == null&&typeCode==null && code==null) {
			activities = activityDao.getAll(limit,page);
		} else if(typeCode!=null && categoryId==null && code==null){
			activities = activityDao.getAllByType(typeCode,limit,page);
			
		} else if(typeCode!=null && categoryId!=null && code==null){
			activities = activityDao.getAllByTypeAndCategory(typeCode,categoryId,limit,page);
		
		} else if(typeCode!=null && categoryId==null && "profile".equals(code)) {
			activities = activityDao.getAllByTypeAndMember(typeCode,principalService.getAuthPrincipal(),limit,page);
		
		} else if(typeCode!=null && categoryId==null && "purchase".equals(code)) {
			
			activities = activityDao.getAllByTypeAndPurchased(typeCode,principalService.getAuthPrincipal(),limit,page);
			userActivities = userActivityDao.getAllById(principalService.getAuthPrincipal());
		} else if(categoryId!=null && typeCode==null && code==null){
			activities = activityDao.getAllByCategory(categoryId,limit,page);
			
		} else if(typeCode!=null && categoryId!=null && "profile".equals(code)) {
			activities = activityDao.getAllByTypeCategoryAndMember(typeCode,categoryId,principalService.getAuthPrincipal(),limit,page);
			
		} else if(typeCode!=null && categoryId!=null && "purchase".equals(code)) {
			activities = activityDao.getAllByTypeCategoryAndPurchased(typeCode,categoryId,principalService.getAuthPrincipal(),limit,page);
			userActivities = userActivityDao.getAllById(principalService.getAuthPrincipal());

		}
		
		List<Schedule> schedules= scheduleDao.getAll();
		List<PojoActivityRes> activitiesRes = new ArrayList<>();

		for (int i = 0; i < activities.size(); i++) {
			PojoActivityRes activity = new PojoActivityRes();
			activity.setActivityCode(activities.get(i).getActivityCode());
			activity.setActivityTypeId(activities.get(i).getActivityType().getId());
			activity.setActivityTypeCode(activities.get(i).getActivityType().getActivityTypeCode());
			activity.setActivityTypeName(activities.get(i).getActivityType().getActivityTypeName());
			activity.setCategoryId(activities.get(i).getCategory().getId());
			activity.setCategoryCode(activities.get(i).getCategory().getCategoryCode());
			activity.setCategoryName(activities.get(i).getCategory().getCategoryName());
			activity.setDescription(activities.get(i).getDescription());
			activity.setTitle(activities.get(i).getTitle());
			activity.setFileId(activities.get(i).getFile().getId());
			activity.setMemberId(activities.get(i).getMember().getId());
			activity.setFullName(activities.get(i).getMember().getProfile().getFullName());
			activity.setId(activities.get(i).getId());
			activity.setLocationAddress(activities.get(i).getLocationAddress());
			activity.setPrice(activities.get(i).getPrice());
			activity.setProvider(activities.get(i).getProvider());
			activity.setTimeEnd(activities.get(i).getTimeEnd());
			activity.setTimeStart(activities.get(i).getTimeStart());
			activity.setVer(activities.get(i).getVer());
			activity.setCreatedAt(DateUtil.dateToStr(activities.get(i).getCreatedAt()) );
			List<PojoScheduleRes> scheduleRes = new ArrayList<>();
			for(int j=0;j<schedules.size();j++) {
				if(schedules.get(j).getActivity().getId().equals(activities.get(i).getId()) ) {
					PojoScheduleRes schedule = new PojoScheduleRes();
					schedule.setId(schedules.get(j).getId());
					schedule.setScheduleDate(schedules.get(j).getScheduleDate());
					schedule.setVer(schedules.get(j).getVer());
					scheduleRes.add(schedule);
				}
			}
		
			activity.setSchedule(scheduleRes);
			if(code!=null&&code.equals("purchase")) {
				
				for(int j=0;j<userActivities.size();j++) {
					
					if(activities.get(i).getId()==userActivities.get(j).getActivity().getId()){
						activity.setIsApprove(userActivities.get(j).getIsApproved());
					}
				}
			}
			activitiesRes.add(activity);
		}
		
		return activitiesRes;
	}

	public PojoRes save(PojoActivityReq data) {
		ConnHandler.begin();
		valIdNull(data);
		valNonBk(data);
		
		Activity activity = new Activity();

		activity.setActivityCode(GenerateCodeUtil.generateCode(10));
		
		ActivityType activityType = activityTypeDao.getById(data.getActivityTypeId()).get();
		activity.setActivityType(activityType);

		Category category = categoryDao.getById(data.getCategoryId()).get();
		activity.setCategory(category);

		activity.setDescription(data.getDescription());
		activity.setTitle(data.getTitle());

		File file = new File();
		file.setFileContent(data.getFile().getFileContent());
		file.setFileExtension(data.getFile().getFileExtension());
		file.setIsActive(true);
		File fileInsert = fileDao.save(file);
		activity.setFile(fileInsert);

		User Member = userDao.getById(principalService.getAuthPrincipal()).get();
		activity.setMember(Member);
		activity.setLocationAddress(data.getLocationAddress());
		activity.setPrice(data.getPrice());
		activity.setProvider(data.getProvider());
		activity.setTimeEnd(OffsetDateTime.parse(data.getTimeEnd()).toLocalTime() );
		activity.setTimeStart(OffsetDateTime.parse(data.getTimeStart()).toLocalTime());
		activity.setIsActive(true);
		
		
		valBkNotNull(activity);
		Activity activityInsert = activityDao.save(activity);

		for (int i = 0; i < data.getSchedule().size(); i++) {
			Schedule schedule = new Schedule();
			schedule.setActivity(activityInsert);
			schedule.setScheduleDate(data.getSchedule().get(i).getScheduleDate());
			schedule.setIsActive(true);
			scheduleDao.save(schedule);
		}

		ConnHandler.commit();

		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage(activity.getActivityType().getActivityTypeName()+ "Created");
		return pojoRes;
	}

	public PojoRes update(PojoActivityReq data) {
		ConnHandler.begin();
		valIdExist(data.getId());
		valIdNotNull(data);

		valNonBk(data);
		Activity activity = new Activity();
		activity = activityDao.getByIdAndDetach(data.getId()).get();
		activity.setTitle(data.getTitle());
		activity.setDescription(data.getDescription());
		activity.setVer(data.getVer());
		activity.setIsActive(true);
		activity.setLocationAddress(data.getLocationAddress());
		activity.setPrice(data.getPrice());
		activity.setProvider(data.getProvider());
		activity.setTimeEnd(OffsetDateTime.parse(data.getTimeEnd()).toLocalTime() );
		activity.setTimeStart(OffsetDateTime.parse(data.getTimeStart()).toLocalTime());
		
		if(data.getSchedule()!=null) {
			
			for (int i = 0; i < data.getSchedule().size(); i++) {
				if(data.getSchedule().get(i).getId()!=null) {
					Schedule schedule = scheduleDao.getByIdAndDetach(data.getSchedule().get(i).getId()).get();
					schedule.setScheduleDate(data.getSchedule().get(i).getScheduleDate());
					schedule.setIsActive(true);
					scheduleDao.save(schedule);
				}else {
					Schedule schedule = new Schedule();
					schedule.setActivity(activity);
					schedule.setScheduleDate(data.getSchedule().get(i).getScheduleDate());
					schedule.setIsActive(true);
					scheduleDao.save(schedule);
				}
				
			}
		}
		
		if(data.getFile()!=null) {
			fileDao.deleteById(File.class, activity.getFile().getId());
			File file =new File();
			file.setFileContent(data.getFile().getFileContent());
			file.setFileExtension(data.getFile().getFileExtension());
			File fileInsert = fileDao.save(file);
			activity.setFile(fileInsert);
		}
		
		activityDao.save(activity);
		ConnHandler.commit();
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Succes Update " + activity.getActivityType().getActivityTypeName());
		return pojoRes;

	}

	@Override
	List<PojoActivityRes> getAll() {
		return null;
	}
	
	public List<PojoActivityTypeRes> getAllType() {
		List<ActivityType> activityTypes = activityTypeDao.getAll();
		List<PojoActivityTypeRes> activityTypeRes = new ArrayList<>();
		for(int i=0;i<activityTypes.size();i++) {
			PojoActivityTypeRes activityType = new PojoActivityTypeRes();
			activityType.setId(activityTypes.get(i).getId());
			activityType.setActivityTypeCode(activityTypes.get(i).getActivityTypeCode());
			activityType.setActivityTypeName(activityTypes.get(i).getActivityTypeName());
			activityTypeRes.add(activityType);
		}
		return activityTypeRes;
	}
	public PojoRes delete(String id) {
		
		try {
			
			ConnHandler.begin();
			valIdExist(id);
			List<String> schedules= scheduleDao.getIdByActivity(id);
			
			for(int i=0;i<schedules.size();i++) {
				System.out.println(schedules);
				scheduleDao.deleteById(Schedule.class,schedules.get(i));
			}
			List<String> userActivityId=userActivityDao.getIdByActivity(id);
			for(int i=0;i<userActivityId.size();i++) {
				userActivityDao.deleteById(UserActivity.class,userActivityId.get(i));
			}
			activityDao.deleteById(Activity.class, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Succes Deleted");
		return pojoRes;
	}
	
	public PojoRes deleteSchedule(String id) {
		
		try {
			ConnHandler.begin();
			valIdExist(id);
			scheduleDao.deleteById(Schedule.class, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Succes Delete Schedule");
		return pojoRes;
	}
	
	
	
	public List<PojoActivityRes> getAllByListCategory(PojoActivityReqGetAll data) {
		List<Activity> activities = new ArrayList<>();
		List<UserActivity>userActivities=new ArrayList<>();
		if (data.getCategory() == null&&data.getType()==null && data.getCode()==null) {
			activities = activityDao.getAll(data.getLimit(),data.getPage());
		} else if(data.getType()!=null && data.getCategory()==null && data.getCode()==null){
			activities = activityDao.getAllByType(data.getType(),data.getLimit(),data.getPage());
			
		} else if(data.getType()!=null && data.getCategory()!=null && data.getCode()==null){
			activities = activityDao.getAllByTypeAndListCategory(data.getType(),data.getCategory(),data.getLimit(),data.getPage());
		
		} else if(data.getType()!=null && data.getCategory()==null && "profile".equals(data.getCode())) {
			activities = activityDao.getAllByTypeAndMember(data.getType(),principalService.getAuthPrincipal(),data.getLimit(),data.getPage());
		
		} else if(data.getType()!=null && data.getCategory()==null && "purchase".equals(data.getCode())) {
			
			activities = activityDao.getAllByTypeAndPurchased(data.getType(),principalService.getAuthPrincipal(),data.getLimit(),data.getPage());
			userActivities = userActivityDao.getAllById(principalService.getAuthPrincipal());
		} else if(data.getCategory()!=null && data.getType()==null && data.getCode()==null){
			activities = activityDao.getAllByListCategory(data.getCategory(),data.getLimit(),data.getPage());
			
		} else if(data.getType()!=null && data.getCategory()!=null && "profile".equals(data.getCode())) {
			activities = activityDao.getAllByTypeListCategoryAndMember(data.getType(),data.getCategory(),principalService.getAuthPrincipal(),data.getLimit(),data.getPage());
			
		} else if(data.getType()!=null && data.getCategory()!=null && "purchase".equals(data.getCode())) {
			activities = activityDao.getAllByTypeListCategoryAndPurchased(data.getType(),data.getCategory(),principalService.getAuthPrincipal(),data.getLimit(),data.getPage());
			userActivities = userActivityDao.getAllById(principalService.getAuthPrincipal());

		}
		
		List<Schedule> schedules= scheduleDao.getAll();
		List<PojoActivityRes> activitiesRes = new ArrayList<>();

		for (int i = 0; i < activities.size(); i++) {
			PojoActivityRes activity = new PojoActivityRes();
			activity.setActivityCode(activities.get(i).getActivityCode());
			activity.setActivityTypeId(activities.get(i).getActivityType().getId());
			activity.setActivityTypeCode(activities.get(i).getActivityType().getActivityTypeCode());
			activity.setActivityTypeName(activities.get(i).getActivityType().getActivityTypeName());
			activity.setCategoryId(activities.get(i).getCategory().getId());
			activity.setCategoryCode(activities.get(i).getCategory().getCategoryCode());
			activity.setCategoryName(activities.get(i).getCategory().getCategoryName());
			activity.setDescription(activities.get(i).getDescription());
			activity.setTitle(activities.get(i).getTitle());
			activity.setFileId(activities.get(i).getFile().getId());
			activity.setMemberId(activities.get(i).getMember().getId());
			activity.setFullName(activities.get(i).getMember().getProfile().getFullName());
			activity.setId(activities.get(i).getId());
			activity.setLocationAddress(activities.get(i).getLocationAddress());
			activity.setPrice(activities.get(i).getPrice());
			activity.setProvider(activities.get(i).getProvider());
			activity.setTimeEnd(activities.get(i).getTimeEnd());
			activity.setTimeStart(activities.get(i).getTimeStart());
			activity.setVer(activities.get(i).getVer());
			activity.setCreatedAt(DateUtil.dateToStr(activities.get(i).getCreatedAt()) );
			List<PojoScheduleRes> scheduleRes = new ArrayList<>();
			for(int j=0;j<schedules.size();j++) {
				if(schedules.get(j).getActivity().getId().equals(activities.get(i).getId()) ) {
					PojoScheduleRes schedule = new PojoScheduleRes();
					schedule.setId(schedules.get(j).getId());
					schedule.setScheduleDate(schedules.get(j).getScheduleDate());
					schedule.setVer(schedules.get(j).getVer());
					scheduleRes.add(schedule);
				}
			}
		
			activity.setSchedule(scheduleRes);
			if(data.getCode()!=null&&data.getCode().equals("purchase")) {
				
				for(int j=0;j<userActivities.size();j++) {
					
					if(activities.get(i).getId()==userActivities.get(j).getActivity().getId()){
						activity.setIsApprove(userActivities.get(j).getIsApproved());
					}
				}
			}
			activitiesRes.add(activity);
		}
		
		return activitiesRes;
	}
}
