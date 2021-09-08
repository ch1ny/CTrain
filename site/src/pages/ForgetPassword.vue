<template>
	<div>
		<el-switch class="switch" v-model="darkStyle" active-color="dimgray" active-icon-class="el-icon-moon" inactive-color="gold" inactive-icon-class="el-icon-sunny" @change="themeChange"></el-switch>
		<el-card class="card-box" :style="{'background': darkStyle?'rgba(10, 10, 10, 0.7)':'rgba(250, 250, 250, 0.7)'}">
			<div slot="header">
				<el-link @click="goBack" :style="{position:'absolute', left:'5%', color:darkStyle?'white':''}"><i class="el-icon-back"></i>后退</el-link>
				<span :style="{'color': darkStyle?'white':'dodgerblue'}">邮箱验证</span>
			</div>
			<el-form :model="formData" :rules="rules" ref="formData">
				<el-form-item prop="id">
					<el-input class="input" @keydown.enter.native="submit" placeholder="请输入注册时的身份证号码" prefix-icon="iconfont icon-credentials_icon" v-model="formData.id"></el-input>
				</el-form-item>
				<el-form-item prop="name">
					<el-input class="input" @keydown.enter.native="submit" placeholder="请输入身份证所有者的姓名" prefix-icon="el-icon-user" v-model="formData.name"></el-input>
				</el-form-item>
				<el-form-item prop="psw">
					<el-input class="input" @keydown.enter.native="submit" placeholder="请输入您的新密码" prefix-icon="el-icon-key" v-model="formData.psw" show-password></el-input>
				</el-form-item>
				<el-form-item prop="captcha">
					<el-input class="input" @keydown.enter.native="submit" placeholder="请输入验证码" prefix-icon="el-icon-bell" v-model="formData.captcha" maxlength="6" show-word-limit></el-input>
				</el-form-item>
				<el-form-item>
					<el-button class="btn" :loading="isBuildingCaptcha" @click="sendCaptcha" :disabled="countdown != 60" plain>{{captcha}}</el-button>
					<el-button class="btn" @mouseover.native="mouseHover" @mouseleave.native="mouseLeave" @click="submit" size="large" type="primary" :style="darkStyle?darkThemeBtnStyle:''" :loading="loading">提交新密码</el-button>
				</el-form-item>
			</el-form>
		</el-card>
		<Ribbons :darkStyle.sync="darkStyle" ref="ribbons" />
	</div>
</template>

<script>
import $ajax from "../ajax";
import Ribbons from "../components/Ribbons.vue";
export default {
	components: {
		Ribbons,
	},
	data() {
		return {
			captcha: "获取验证码",
			countdown: 60,
			formData: {
				id: "",
				name: "",
				psw: "",
				captcha: "",
			},
			loading: false,
			isBuildingCaptcha: false,
			darkStyle: false,
			darkThemeBtnStyle:
				"background-color: gold; border-color: gold; color: #1f1f1f",
			rules: {
				id: [
					{
						required: true,
						message: "请输入身份证号",
						trigger: "blur",
					},
				],
				name: [
					{ required: true, message: "请输入姓名", trigger: "blur" },
				],
				psw: [
					{ required: true, message: "请输入密码", trigger: "blur" },
				],
				captcha: [
					{
						required: true,
						message: "请输入验证码",
						trigger: "blur",
					},
				],
			},
		};
	},
	methods: {
		mouseHover() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: yellow; border-color: yellow; color: #1f1f1f";
			}
		},
		mouseLeave() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: gold; border-color: gold; color: #1f1f1f";
			}
		},
		goBack() {
			this.$router.go(-1);
		},
		themeChange() {
			this.$nextTick(() => {
				this.$refs.ribbons.themeChange();
			});
		},
		async sendCaptcha() {
			if (this.formData.id == "") {
				this.$notify({
					title: "",
					message: "请输入注册时使用的身份证！",
					type: "error",
					showClose: false,
					duration: 1000,
				});
			} else {
				this.isBuildingCaptcha = true;
				this.captcha = "发送验证码";
				let res = await $ajax.post(
					"/user/sendFPC",
					{
						user: this.formData.id,
					},
					{
						headers: {
							Authorization:
								"Bearer " + localStorage.getItem("token"),
						},
					}
				);
				this.isBuildingCaptcha = false;
				if (res.data.code == 0) {
					this.$notify({
						title: "验证码发送成功",
						message:
							"已将验证码发送至您注册时使用的邮箱" +
							res.data.data +
							"中，请前往邮箱查看",
						type: "success",
						duration: 3000,
					});
					this.captcha = "重新发送(" + this.countdown + "秒)";
					this.countdown--;
					let interval = window.setInterval(() => {
						this.captcha = "重新发送(" + this.countdown + "秒)";
						this.countdown--;
						if (this.countdown < 0) {
							this.captcha = "重新发送";
							this.countdown = 60;
							window.clearInterval(interval);
						}
					}, 1000);
				} else {
					this.$notify({
						title: "验证码发送失败",
						message: res.data.data,
						type: "error",
						duration: 3000,
					});
					this.captcha = "重新发送";
				}
			}
		},
		submit() {
			this.$refs["formData"].validate(async (valid) => {
				if (valid) {
					this.loading = true;
					let res = await $ajax.post("/user/forgetPsw", {
						user: this.formData.id,
						name: this.formData.name,
						psw: this.formData.psw,
						captcha: this.formData.captcha,
					});
					switch (res.data.code) {
						case 0:
							this.$notify({
								title: "密码修改成功",
								message: "正在前往登录页面...",
								type: "success",
								duration: 1000,
								showClose: false,
								onClose: () => {
									this.$router.push({ path: "/login" });
								},
							});
							break;
						default:
							this.$notify({
								title: "密码修改失败",
								message: `${res.data.result}`,
								type: "error",
								duration: 1500,
								showClose: false,
							});
							break;
					}
					this.loading = false;
				} else {
					console.log("error submit!!");
					return false;
				}
			});
		},
	},
	created() {
		if (localStorage.getItem("token") != null) {
			this.$router.replace("/");
		}
	},
};
</script>

<style scoped>
div {
	text-align: center;
}
.switch {
	position: absolute;
	right: 5%;
	top: 5%;
}
.input {
	width: 100%;
	margin-top: 5%;
}
.card-box {
	width: 30%;
	height: 70%;
	position: absolute;
	left: 35%;
	top: 15%;
}
.btn {
	width: 30%;
	height: 50px;
	margin-block: 5%;
	margin-inline: 5%;
	text-align: center;
	padding: 1%;
}
</style>