package com.lawencon.leaf.community.dao;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.model.UserPremium;

@Repository
public class UserPremiumDao extends BaseDao<UserPremium> {

	@SuppressWarnings("unchecked")
	public List<UserPremium> getAll(int limit, int offset) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * ");
		sql.append("FROM t_user_premium ");
		sql.append("ORDER BY created_at DESC");
		final List<UserPremium> activities = ConnHandler.getManager().createNativeQuery(sql.toString(), UserPremium.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();
		return activities;
	}

	@SuppressWarnings("unchecked")
	public List<UserPremium> getAllNonApproved(int limit, int offset) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * ");
		sql.append("FROM t_user_premium ");
		sql.append("WHERE expire_date IS NULL ");
		sql.append("AND is_active =true ");
		sql.append("ORDER BY created_at DESC");

		final List<UserPremium> activities = ConnHandler.getManager().createNativeQuery(sql.toString(), UserPremium.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();
		return activities;
	}
	
	@Override
	public Optional<UserPremium> getByIdAndDetach(String id) {
		return Optional.ofNullable(super.getByIdAndDetach(UserPremium.class, id));
	}

	@Override
	public Optional<UserPremium> getById(String id) {

		return Optional.ofNullable(super.getById(UserPremium.class, id));

	}
	
	public Optional<UserPremium> getByUserPurchased(String memberId) {

		UserPremium userPremium=null;
		
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT id, expire_date ");
		sql.append("FROM t_user_premium ");
		sql.append("WHERE member_id=:memberId ");
		sql.append("AND NOW()< expire_date  ");
		sql.append("AND is_active = true");

		try {
			final Object result = ConnHandler.getManager().createNativeQuery(sql.toString())
					.setParameter("memberId", memberId).getSingleResult();

			if (result != null) {
				userPremium = new UserPremium();
				final Object[] objArr = (Object[]) result;

				userPremium.setId(objArr[0].toString());
				userPremium.setExpireDate(Date.valueOf(objArr[1].toString()).toLocalDate() );
			}
		} catch (Exception e) {
		}
		
		return Optional.ofNullable(userPremium);

	}
	
	public Optional<UserPremium> getByUser(String memberId) {

		UserPremium userPremium=null;
		
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT id, expire_date ");
		sql.append("FROM t_user_premium ");
		sql.append("WHERE member_id=:memberId ");
		sql.append("AND expire_date = NULL ");
		sql.append("AND is_active = true");

		try {
			final Object result = ConnHandler.getManager().createNativeQuery(sql.toString())
					.setParameter("memberId", memberId).getSingleResult();

			if (result != null) {
				userPremium = new UserPremium();
				final Object[] objArr = (Object[]) result;

				userPremium.setId(objArr[0].toString());
				userPremium.setExpireDate(Date.valueOf(objArr[0].toString()).toLocalDate() );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Optional.ofNullable(userPremium);

	}

	@Override
	List<UserPremium> getAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Long countUserPremium() {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(a.id) ");
		sql.append("FROM t_user_premium a ");
		final Long count = Long.valueOf(ConnHandler.getManager()
				.createNativeQuery(sql.toString())
				.getSingleResult().toString());
		return count;
	}

}
