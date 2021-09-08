<template>
	<div id="app">
		<keep-alive>
			<router-view v-if="$route.meta.isKeepAlive" />
		</keep-alive>
		<router-view v-if="!$route.meta.isKeepAlive" />
	</div>
</template>

<script>
export default {
	methods: {
		isMobile() {
			let flag = navigator.userAgent.match(
				/(phone|pad|pod|iPhone|iPod|ios|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i
			);
			return flag;
		},
	},
	created() {
		console.log(
			"\n ██████╗████████╗██████╗  █████╗ ██╗███╗   ██╗\n██╔════╝╚══██╔══╝██╔══██╗██╔══██╗██║████╗  ██║\n██║        ██║   ██████╔╝███████║██║██╔██╗ ██║\n██║        ██║   ██╔══██╗██╔══██║██║██║╚██╗██║\n╚██████╗   ██║   ██║  ██║██║  ██║██║██║ ╚████║\n ╚═════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝"
		);
		if (localStorage.getItem("darkTheme") == null) {
			localStorage.setItem("darkTheme", false);
		}
		if (localStorage.getItem("token") != null) {
			let jwt = require("jsonwebtoken");
			const token = jwt.decode(localStorage.getItem("token"));
			let exp = token.exp * 1000;
			if (exp <= new Date().getTime()) {
				this.$message({
					message: "您的身份认证已过期，请重新登陆",
					type: "warning",
					duration: 1000,
					onClose: () => {
						localStorage.removeItem("token");
						this.$router.push("/login");
					},
				});
			}
		}
	},
	mounted() {
		if (this.isMobile()) {
			this.$router.push({ path: "/error/mobile" });
		}
	},
	watch: {
		$route(to) {
			if (to.path != "/error/mobile" && this.isMobile()) {
				this.$router.push({ path: "/error/mobile" });
			}
		},
	},
};
</script>

<style>
@import "./assets/fonts/fonts.css";
* {
	margin: 0%;
}
#app {
	font-family: "JZJDKT";
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
	text-align: center;
	color: #2c3e50;
}
</style>
