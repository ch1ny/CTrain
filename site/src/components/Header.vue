<template>
	<div>
		<el-header style="text-align: right; background: rgba(250, 250, 250, 0.5); height: 10vh; line-height: 10vh">
			<div v-if="noToken">
				<el-button plain @click="Register">注册</el-button>
				<el-button @click="Login" type="primary">登录</el-button>
			</div>
			<div v-if="!noToken">
				<el-dropdown class="drop" :show-timeout="0">
					<div style="margin-right: 30px" @mouseenter="nameColor='dodgerblue';" @mouseleave="nameColor='#000';">
						<el-avatar style="vertical-align: middle" :src="avatar"><i style="font-size: 25px; vertical-align: middle;" class="el-icon-loading"></i></el-avatar>
						<el-link :underline="false" :style="{'font-size': '25px', 'font-family': 'KaiTi', 'margin-left': '5px', 'color': nameColor}">{{ username }}</el-link>
					</div>
					<el-dropdown-menu>
						<el-dropdown-item @click.native="myInfo"><i class="el-icon-user"></i>个人信息</el-dropdown-item>
						<el-dropdown-item @click.native="goToMyIndent"><i class="el-icon-tickets"></i>我的订单</el-dropdown-item>
						<el-dropdown-item @click.native="goToAdmin" v-if="amAdmin"><i class="el-icon-medal"></i>后台管理</el-dropdown-item>
						<el-dropdown-item @click.native="Logout"><i class="el-icon-switch-button"></i>退出登录</el-dropdown-item>
					</el-dropdown-menu>
				</el-dropdown>
			</div>
		</el-header>
	</div>
</template>

<script>
import $ajax from "../ajax";
export default {
	name: "myHeader",
	data() {
		return {
			noToken: true,
			username: "",
			avatar: localStorage.getItem("avatar"),
			nameColor: "#000",
			amAdmin: false,
		};
	},
	methods: {
		Login() {
			this.$router.push({ path: "/login" });
		},
		Logout() {
			localStorage.removeItem("token");
			localStorage.removeItem("avatar");
			this.$router.go(0);
		},
		myInfo() {
			this.$router.push({ path: "/info" });
		},
		goToMyIndent() {
			this.$router.push({ path: "/indent" });
		},
		Register() {
			this.$router.push({ path: "/register" });
		},
		goToAdmin() {
			this.$router.push({ path: "/admin" });
		},
		async init() {
			this.noToken = localStorage.getItem("token") == null;
			if (!this.noToken) {
				let jsonwebtoken = require("jsonwebtoken");
				let token = jsonwebtoken.decode(localStorage.getItem("token"));
				this.username = token.aud;
				let userId = token.sub;
				let res = await $ajax.post(
					"/admin/vertify",
					{},
					{
						headers: {
							Authorization:
								"Bearer " + localStorage.getItem("token"),
						},
					}
				);
				if (res.data.code == 0) {
					this.amAdmin = true;
				}
				if (this.avatar == null) {
					res = await $ajax.post(
						"/user/getThumbnailAvatar",
						{},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0 && res.data.data != "") {
						this.avatar = res.data.data;
						localStorage.setItem("avatar", res.data.data);
					} else {
						let sexCode = Number(
							userId.substr(userId.length - 2, 1)
						);
						if (sexCode % 2 == 0) {
							this.avatar = require("@/assets/icon/female.png");
						} else {
							this.avatar = require("@/assets/icon/male.png");
						}
					}
				} else {
					this.avatar = localStorage.getItem("avatar");
					res = await $ajax.post(
						"/user/getThumbnailAvatar",
						{},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0 && res.data.data != "") {
						this.avatar = res.data.data;
						localStorage.setItem("avatar", res.data.data);
					} else {
						let sexCode = Number(
							userId.substr(userId.length - 2, 1)
						);
						if (sexCode % 2 == 0) {
							this.avatar = require("@/assets/icon/female.png");
						} else {
							this.avatar = require("@/assets/icon/male.png");
						}
					}
				}
			}
		},
	},
	created() {
		this.init();
	},
};
</script>

<style>
.drop:hover {
	cursor: pointer;
}
</style>
