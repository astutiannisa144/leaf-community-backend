package com.lawencon.leaf.community.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lawencon.base.AbstractJpaDao;
import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.constant.EnumRole;
import com.lawencon.leaf.community.dao.ActivityDao;
import com.lawencon.leaf.community.dao.UserActivityDao;
import com.lawencon.leaf.community.dao.UserDao;
import com.lawencon.leaf.community.model.Activity;
import com.lawencon.leaf.community.model.File;
import com.lawencon.leaf.community.model.Industry;
import com.lawencon.leaf.community.model.Profile;
import com.lawencon.leaf.community.model.User;
import com.lawencon.leaf.community.model.UserActivity;
import com.lawencon.leaf.community.model.UserVoucher;
import com.lawencon.leaf.community.model.Voucher;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.user.activity.PojoUserActivityReq;
import com.lawencon.leaf.community.pojo.user.activity.PojoUserActivityRes;
import com.lawencon.leaf.community.util.GenerateCodeUtil;
import com.lawencon.security.principal.PrincipalService;
import com.lawencon.security.principal.PrincipalServiceImpl;

@Service
public class UserActivityService extends AbstractJpaDao {
	@Autowired
	private UserActivityDao userActivityDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ActivityDao activityDao;
	@Autowired
	private final PrincipalService principalService;
	
	private static final BigDecimal percent = new BigDecimal(0.8);
	
	public UserActivityService(PrincipalServiceImpl principalServiceImpl) {
		this.principalService = principalServiceImpl;
	}
	
	private void valIdNull(UserActivity userActivity) {
		if(userActivity.getId()!=null) {
			throw new RuntimeException("Id Harus Kosong");
		}
	}

	private void valBkNotExist(UserActivity userActivity) {
		if(userActivityDao.getByCode(userActivity.getInvoiceCode()).isPresent()) {
			throw new RuntimeException("Invoice Code Already Exist");
		}
	}
	private void valBkNotNull(UserActivity userActivity) {
		if(userActivity.getInvoiceCode()==null) {
			throw new RuntimeException("Invoice Code Tidak Boleh Kosong");
		}
	}
	private void valBkNotChange(UserActivity userActivity) {
		if(userActivityDao.getById(userActivity.getId()).get().getInvoiceCode()!=userActivity.getInvoiceCode()) {
			throw new RuntimeException("Invoice Code Cant Change Exist");
		}
	}

	private void valNonBk(UserActivity userActivity) {
		if(userActivity.getMember()==null) {
			throw new RuntimeException("Member Tidak Boleh Kosong");
		}
		if(userActivity.getTotalPrice()==null) {
			throw new RuntimeException("Price Tidak Boleh Kosong");
		}
		if(userActivity.getActivity()==null) {
			throw new RuntimeException("Activity Tidak Boleh Kosong");
		}
		if((userActivity.getIsActive()==null)) {
			throw new RuntimeException("Active Tidak Boleh Kosong");

		}
		
	}
	private void valIdNotNull(UserActivity userActivity) {
		if(userActivity.getId()==null) {
			throw new RuntimeException("Id Tidak Boleh Kosong");
		}
	}
	
	private void valIdExist(String id) {
		if(userActivityDao.getById(id).isEmpty()) {
			throw new RuntimeException("Id Tidak Boleh Kosong");
		}
	}
	
	
	
	public PojoRes insert(PojoUserActivityReq data) {
		ConnHandler.begin();
		String code = GenerateCodeUtil.generateCode(10);
		final UserActivity userActivity = new UserActivity();
		final User member = userDao.getByIdRef(User.class, principalService.getAuthPrincipal());
		final Activity activity = activityDao.getByIdAndDetach(Activity.class, data.getActivityId());
		if (data.getUserVoucher() != null) {
			Voucher voucher = getById(Voucher.class, data.getUserVoucher().getVoucherId());

			if (voucher.getMinimumPurchase().compareTo(activity.getPrice())==1) {
				throw new RuntimeException("This Voucher Cant be used for this purchase");
			}
			UserVoucher userVoucher = new UserVoucher();
			userVoucher.setMember(member);
			userVoucher.setVoucher(voucher);
			UserVoucher userVoucherInsert = save(userVoucher);
			userActivity.setTotalPrice(activity.getPrice().subtract(voucher.getDiscountPrice()));
			userActivity.setUserVoucher(userVoucherInsert);
		} else {
			userActivity.setTotalPrice(activity.getPrice());
		}
		File file = new File();
		file.setFileContent(data.getFile().getFileContent());
		file.setFileExtension(data.getFile().getFileExtension());
		file.setIsActive(true);
		File fileInsert = save(file);
		userActivity.setInvoiceCode(code);
		userActivity.setFile(fileInsert);
		userActivity.setActivity(activity);
		userActivity.setMember(member);
		userActivity.setIsActive(true);
		
		valIdNull(userActivity);
		valNonBk(userActivity);
		valBkNotNull(userActivity);
		valBkNotExist(userActivity);
		userActivityDao.save(userActivity);

		ConnHandler.commit();

		final PojoRes res = new PojoRes();
		res.setMessage("Success insert UserActivity");
		return res;
	}

	

	public PojoRes update(PojoUserActivityReq data) {
		ConnHandler.begin();
		
		final UserActivity userActivity = userActivityDao.getByIdAndDetach(UserActivity.class, data.getId());
		if(userActivity.getVer()>0) {
			throw new RuntimeException("Anda Sudah Approve Transaksi ini TIdak bisa approve lagi");
		}
		User admin =userDao.getUserByRole(EnumRole.SY.getCode()).get();
		final Activity activity = activityDao.getByIdAndDetach(Activity.class, userActivity.getActivity().getId());
		final Profile profileAdmin = getByIdAndDetach(Profile.class, admin.getProfile().getId());
		final User member = userDao.getById(User.class, activity.getMember().getId());
		final Profile profileMember = getByIdAndDetach(Profile.class, member.getProfile().getId());
		BigDecimal priceMember = activity.getPrice().multiply(percent);
		BigDecimal memberBalance = profileMember.getBalance().add(priceMember);
		BigDecimal adminBalance = profileAdmin.getBalance().add(userActivity.getTotalPrice().subtract(priceMember));
		
		profileMember.setBalance(memberBalance);
		profileAdmin.setBalance(adminBalance);
		save(profileAdmin);
		save(profileMember);
		userActivity.setIsApproved(true);
		userActivity.setIsActive(true);
		
		valIdExist(userActivity.getId());
		valIdNotNull(userActivity);
		valBkNotNull(userActivity);
		valBkNotChange(userActivity);
		valNonBk(userActivity);
		
		userActivityDao.save(userActivity);

		ConnHandler.commit();

		final PojoRes res = new PojoRes();
		res.setMessage("Success Approved UserActivity");
		return res;
	}

	public PojoUserActivityRes getById(String id) {
		UserActivity userActivity = userActivityDao.getById(id).get();
		final PojoUserActivityRes pojoUserActivityRes = new PojoUserActivityRes();
		pojoUserActivityRes.setActivityName(userActivity.getActivity().getTitle());
		pojoUserActivityRes.setFileId(userActivity.getFile().getId());
		pojoUserActivityRes.setId(userActivity.getId());
		pojoUserActivityRes.setIsApprove(userActivity.getIsApproved());
		pojoUserActivityRes.setMemberName(userActivity.getMember().getProfile().getFullName());
		pojoUserActivityRes.setTotalPrice(userActivity.getTotalPrice());
		pojoUserActivityRes.setVer(userActivity.getVer());
		if (userActivity.getUserVoucher() != null) {
			pojoUserActivityRes.setVoucherCode(userActivity.getUserVoucher().getVoucher().getVoucherCode());
		}
		pojoUserActivityRes.setIsActive(userActivity.getIsActive());
		pojoUserActivityRes.setInvoiceCode(userActivity.getInvoiceCode());

		return pojoUserActivityRes;
	}

	public List<PojoUserActivityRes> getAll(String typeCode, String code) {
		final List<PojoUserActivityRes> pojoUserActivityRes = new ArrayList<>();
		List<UserActivity> userActivities = new ArrayList<>();

		if(code==null && typeCode==null) {
			userActivities = userActivityDao.getAll();
		
		}else if (typeCode != null && code==null) {
			userActivities = userActivityDao.getAllByActivityType(typeCode);
		} else if (code.equals("profile") && typeCode == null) {

			userActivities = userActivityDao.getAllByActivityPurchased(principalService.getAuthPrincipal());
		} else if (code.equals("profile") && typeCode != null) {
			userActivities = userActivityDao.getAllByActivityByTypeAndMember(typeCode,principalService.getAuthPrincipal());
		
		}  else if (code.equals("non") && typeCode != null) {
			userActivities = userActivityDao.getAllByActivityTypeNotPurchase(typeCode);
		}

		for (int i = 0; i < userActivities.size(); i++) {
			PojoUserActivityRes userActivity = new PojoUserActivityRes();
			userActivity.setActivityName(userActivities.get(i).getActivity().getTitle());
			userActivity.setActivityId(userActivities.get(i).getActivity().getId());
			userActivity.setFileId(userActivities.get(i).getFile().getId());
			userActivity.setId(userActivities.get(i).getId());
			userActivity.setIsApprove(userActivities.get(i).getIsApproved());
			userActivity.setMemberName(userActivities.get(i).getMember().getProfile().getFullName());
			userActivity.setTotalPrice(userActivities.get(i).getTotalPrice());
			userActivity.setVer(userActivities.get(i).getVer());
			if (userActivities.get(i).getUserVoucher() != null) {
				userActivity.setVoucherCode(userActivities.get(i).getUserVoucher().getVoucher().getVoucherCode());
			}
			userActivity.setInvoiceCode(userActivities.get(i).getInvoiceCode());
			userActivity.setIsActive(userActivities.get(i).getIsActive());

			pojoUserActivityRes.add(userActivity);
		}
		return pojoUserActivityRes;
	}

	public PojoRes delete(String id) {

		try {
			ConnHandler.begin();
			valIdExist(id);
			userActivityDao.deleteById(Industry.class, id);
		} catch (Exception e) {
			e.printStackTrace();

		}
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Succes Delete User Activity");
		return pojoRes;
	}

}
