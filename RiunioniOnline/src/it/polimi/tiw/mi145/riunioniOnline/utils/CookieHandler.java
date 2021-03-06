package it.polimi.tiw.mi145.riunioniOnline.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.mi145.riunioniOnline.beans.Session;
import it.polimi.tiw.mi145.riunioniOnline.dao.SessionDAO;

public class CookieHandler {

	public static Integer getUserIdByCookie(HttpServletRequest request, Connection connection) throws SQLException {
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null)
			return null;

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("session")) {

				SessionDAO sessionDAO = new SessionDAO(connection);
				Session session = sessionDAO.getSessionById(StringEscapeUtils.escapeJava(cookie.getValue()));
				if (session == null) {
					return null;
				}
				if (new Date().getTime() - session.getDate().getTime() < 1000 * 60 * 60 * 24 * 2) {
					return session.getUser();
				} else {
					sessionDAO.removeSessionById(cookie.getValue());
					return null;
				}
			}
		}
		return null;
	}

	public static Cookie getValidCookieByUserId(int userId, Connection connection) throws SQLException {
		SessionDAO sessionDAO = new SessionDAO(connection);

		sessionDAO.removeSessionByUserId(userId);

		return new Cookie("session", sessionDAO.addSession(userId));
	}

	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, Connection connection)
			throws SQLException {
		
		Integer userId = getUserIdByCookie(request, connection);
		if(userId == null)
			return;
		
		SessionDAO sessionDAO = new SessionDAO(connection);
		sessionDAO.removeSessionByUserId(userId);

		Cookie cookie = new Cookie("session", "");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}